package com.koreait.moviesite.RankingGenreBoard.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.koreait.moviesite.RankingGenreBoard.dto.BoardDtos;
import com.koreait.moviesite.RankingGenreBoard.entity.BoardPostImage;
import com.koreait.moviesite.RankingGenreBoard.service.BoardImageService;
import com.koreait.moviesite.RankingGenreBoard.service.BoardService;

@Controller
@RequestMapping("/board")
public class BoardController {

    private static final Logger log = LoggerFactory.getLogger(BoardController.class);

    private final BoardService boardService;
    private final BoardImageService boardImageService;

    public BoardController(BoardService boardService, BoardImageService boardImageService) {
        this.boardService = boardService;
        this.boardImageService = boardImageService;
    }

    // ======================
    // 목록
    // ======================
    @GetMapping
    public String board(@RequestParam(name = "page", defaultValue = "1") int page,
                        Model model) {

        int safePage = Math.max(page, 1);
        int size = 10;

        Page<BoardDtos.PostResponse> postsPage = boardService.list(
                PageRequest.of(safePage - 1, size, Sort.by(Sort.Direction.DESC, "id"))
        );

        model.addAttribute("posts", postsPage.getContent());
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", postsPage.getTotalPages());
        model.addAttribute("totalElements", postsPage.getTotalElements());

        return "RankingGenreboard/board";
    }

    // ======================
    // 상세
    // ======================
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id,
                         @RequestParam(name = "page", defaultValue = "1") int backPage,
                         HttpSession session,
                         Model model) {

        BoardDtos.PostResponse post = boardService.get(id, true);

        List<BoardPostImage> images = boardImageService.listByPostId(id);
        List<String> imageUrls = resolveImageUrls(images);

        String loginId = (String) session.getAttribute("loginId");
        boolean isOwner = (loginId != null && !loginId.isBlank() && loginId.equals(post.author()));

        model.addAttribute("post", post);
        model.addAttribute("images", images);
        model.addAttribute("imageUrls", imageUrls);
        model.addAttribute("backPage", backPage);
        model.addAttribute("isOwner", isOwner);

        // FlashAttribute로 넘어오는 값들(있으면 boarddetail에서 배너로 표시)
        // uploadAttempted / uploadSaved / uploadWarnings / postActionMsg

        log.info("detail id={}, images.size={}", id, images == null ? 0 : images.size());

        return "RankingGenreboard/boarddetail";
    }

    // ======================
    // 글쓰기 화면
    // ======================
    @GetMapping("/write")
    public String writeForm(Model model, HttpSession session) {
        String loginId = (String) session.getAttribute("loginId");
        if (loginId == null || loginId.isBlank()) {
            return "redirect:/login";
        }
        model.addAttribute("form", new BoardWriteRequest());
        return "RankingGenreboard/write";
    }

    // ======================
    // 글쓰기 처리 (이미지 포함)
    // - 가능한 것만 저장 + 경고는 Flash로 상세 배너
    // ======================
    @PostMapping("/write")
    public String writeSubmit(@Valid @ModelAttribute("form") BoardWriteRequest form,
                              BindingResult bindingResult,
                              @RequestParam(value = "images", required = false) List<MultipartFile> images,
                              @RequestParam(value = "images[]", required = false) List<MultipartFile> imagesBracket,
                              HttpSession session,
                              RedirectAttributes ra) {

        String loginId = (String) session.getAttribute("loginId");
        if (loginId == null || loginId.isBlank()) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "RankingGenreboard/write";
        }

        // 1) 글 저장
        Long postId = boardService.create(new BoardDtos.PostCreateRequest(
                form.getTitle(), form.getContent(), loginId
        ));

        // 2) 이미지 합치기 + 빈 파일 제거
        List<MultipartFile> filtered = mergeAndFilter(images, imagesBracket);

        log.info("==== /board/write POST ====");
        log.info("loginId={}, postId={}, titleLen={}, contentLen={}",
                loginId, postId,
                form.getTitle() == null ? 0 : form.getTitle().length(),
                form.getContent() == null ? 0 : form.getContent().length());
        log.info("images.size(raw)={}, images[].size(raw)={}, filtered.size={}",
                images == null ? 0 : images.size(),
                imagesBracket == null ? 0 : imagesBracket.size(),
                filtered.size());

        // 3) 이미지 저장(부분 저장) + 경고 Flash
        if (!filtered.isEmpty()) {
            try {
                BoardImageService.ImageSaveResult r =
                        boardImageService.saveImages(postId, filtered, BoardImageService.MAX_COUNT);

                ra.addFlashAttribute("uploadAttempted", r.attempted());
                ra.addFlashAttribute("uploadSaved", r.saved());
                ra.addFlashAttribute("uploadWarnings", r.warnings());

            } catch (Exception e) {
                log.error("Image save failed. postId={}", postId, e);
                ra.addFlashAttribute("uploadAttempted", filtered.size());
                ra.addFlashAttribute("uploadSaved", 0);
                ra.addFlashAttribute("uploadWarnings",
                        List.of("이미지 저장 중 서버 오류가 발생했어요. (콘솔 로그 확인)"));
            }
        }

        return "redirect:/board/" + postId + "?page=1";
    }

    // ======================
    // 수정 화면
    // ======================
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id,
                           @RequestParam(name = "page", defaultValue = "1") int backPage,
                           HttpSession session,
                           Model model) {

        String loginId = (String) session.getAttribute("loginId");
        if (loginId == null || loginId.isBlank()) return "redirect:/login";

        BoardDtos.PostResponse post = boardService.get(id, false);
        if (!loginId.equals(post.author())) {
            // 권한 없음
            return "redirect:/board/" + id + "?page=" + backPage;
        }

        List<BoardPostImage> images = boardImageService.listByPostId(id);

        BoardEditRequest form = new BoardEditRequest();
        form.setTitle(post.title());
        form.setContent(post.content());

        model.addAttribute("postId", id);
        model.addAttribute("backPage", backPage);
        model.addAttribute("form", form);
        model.addAttribute("images", images);

        return "RankingGenreboard/boardedit";
    }

    // ======================
    // 수정 처리
    // ✅ 기존 이미지 삭제
    // ✅ 신규 이미지 추가(남은 슬롯만 저장)
    // ✅ 드래그 순서(imageOrder) 반영 -> display_order 업데이트
    // ✅ 경고는 Flash로 상세 배너 표시
    // ======================
    @PostMapping("/{id}/edit")
    public String editSubmit(@PathVariable("id") Long id,
                             @RequestParam(name = "page", defaultValue = "1") int backPage,
                             @Valid @ModelAttribute("form") BoardEditRequest form,
                             BindingResult bindingResult,

                             // 기존 이미지 삭제 체크
                             @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds,

                             // 신규 이미지 추가
                             @RequestParam(value = "images", required = false) List<MultipartFile> images,
                             @RequestParam(value = "images[]", required = false) List<MultipartFile> imagesBracket,

                             // ✅ 드래그 순서 (예: "21,22,25,23,24")
                             @RequestParam(value = "imageOrder", required = false) String imageOrder,

                             HttpSession session,
                             RedirectAttributes ra,
                             Model model) {

        String loginId = (String) session.getAttribute("loginId");
        if (loginId == null || loginId.isBlank()) return "redirect:/login";

        BoardDtos.PostResponse post = boardService.get(id, false);
        if (!loginId.equals(post.author())) {
            return "redirect:/board/" + id + "?page=" + backPage;
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("postId", id);
            model.addAttribute("backPage", backPage);
            model.addAttribute("images", boardImageService.listByPostId(id));
            return "RankingGenreboard/boardedit";
        }

        // 1) 글 업데이트
        boardService.update(id, new BoardDtos.PostUpdateRequest(form.getTitle(), form.getContent()));

        // 2) 삭제 체크된 이미지 삭제
        BoardImageService.ImageDeleteResult del =
                boardImageService.deleteImages(id, deleteImageIds);

        // 3) 남은 슬롯 계산
        long remainExisting = boardImageService.countByPostId(id);
        int remainingSlots = (int) Math.max(0, BoardImageService.MAX_COUNT - remainExisting);

        // 4) 신규 이미지 저장(남은 슬롯만)
        List<MultipartFile> filtered = mergeAndFilter(images, imagesBracket);

        BoardImageService.ImageSaveResult savedResult =
                new BoardImageService.ImageSaveResult(0, 0, List.of());

        if (!filtered.isEmpty() && remainingSlots > 0) {
            savedResult = boardImageService.saveImages(id, filtered, remainingSlots);
        } else if (!filtered.isEmpty() && remainingSlots == 0) {
            // 슬롯 0인데 업로드 시도 -> 전부 초과 경고로
            List<String> warn = new ArrayList<>();
            for (MultipartFile f : filtered) {
                String name = (f.getOriginalFilename() == null || f.getOriginalFilename().isBlank())
                        ? "image" : f.getOriginalFilename();
                warn.add("최대 " + BoardImageService.MAX_COUNT + "장 초과로 제외: " + name);
            }
            savedResult = new BoardImageService.ImageSaveResult(filtered.size(), 0, warn);
        }

        // 5) ✅ 드래그 순서 반영 (삭제된 이미지 제외하고 적용)
        // imageOrder가 없으면 reorder를 건드리지 않음(기존 순서 유지)
        if (imageOrder != null && !imageOrder.isBlank()) {
            List<Long> orderedIds = parseIdsCsv(imageOrder);

            // 삭제된 id는 순서 목록에서 제거 (안전)
            if (deleteImageIds != null && !deleteImageIds.isEmpty() && !orderedIds.isEmpty()) {
                orderedIds = orderedIds.stream()
                        .filter(x -> x != null && !deleteImageIds.contains(x))
                        .toList();
            }

            try {
                boardImageService.reorderImages(id, orderedIds);
            } catch (Exception e) {
                log.error("reorderImages failed. postId={}, imageOrder={}", id, imageOrder, e);
            }
        }

        // 6) 경고 합치기(삭제 경고 + 업로드 경고)
        List<String> allWarnings = new ArrayList<>();
        if (del.warnings() != null) allWarnings.addAll(del.warnings());
        if (savedResult.warnings() != null) allWarnings.addAll(savedResult.warnings());

        ra.addFlashAttribute("uploadAttempted", savedResult.attempted());
        ra.addFlashAttribute("uploadSaved", savedResult.saved());
        ra.addFlashAttribute("uploadWarnings", allWarnings);

        ra.addFlashAttribute("postActionMsg",
                "수정 완료 (삭제 " + del.deleted() + "장, 신규 " + savedResult.saved() + "장 저장)");

        return "redirect:/board/" + id + "?page=" + backPage;
    }

    // ======================
    // 삭제 처리
    // ======================
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id,
                         @RequestParam(name = "page", defaultValue = "1") int backPage,
                         HttpSession session,
                         RedirectAttributes ra) {

        String loginId = (String) session.getAttribute("loginId");
        if (loginId == null || loginId.isBlank()) return "redirect:/login";

        BoardDtos.PostResponse post = boardService.get(id, false);
        if (!loginId.equals(post.author())) {
            return "redirect:/board/" + id + "?page=" + backPage;
        }

        // 이미지도 같이 제거(파일/DB 정리)
        try {
            List<BoardPostImage> imgs = boardImageService.listByPostId(id);
            if (imgs != null && !imgs.isEmpty()) {
                List<Long> ids = imgs.stream().map(BoardPostImage::getId).toList();
                boardImageService.deleteImages(id, ids);
            }
        } catch (Exception e) {
            log.warn("deleteImages before post delete failed. postId={}", id, e);
        }

        boardService.delete(id);
        ra.addFlashAttribute("postActionMsg", "삭제 완료");

        return "redirect:/board?page=" + backPage;
    }

    // -----------------------------
    // helpers: images merge/filter
    // -----------------------------
    private List<MultipartFile> mergeAndFilter(List<MultipartFile> images,
                                               List<MultipartFile> imagesBracket) {
        List<MultipartFile> all = new ArrayList<>();
        if (images != null) all.addAll(images);
        if (imagesBracket != null) all.addAll(imagesBracket);

        return all.stream().filter(f -> f != null && !f.isEmpty()).toList();
    }

    // -----------------------------
    // helpers: imageOrder parse
    // 예: "21, 22, 25" -> [21,22,25]
    // -----------------------------
    private List<Long> parseIdsCsv(String csv) {
        if (csv == null) return List.of();
        String s = csv.trim();
        if (s.isBlank()) return List.of();

        String[] parts = s.split(",");
        List<Long> out = new ArrayList<>();
        for (String p : parts) {
            String t = p == null ? "" : p.trim();
            if (t.isBlank()) continue;
            try {
                out.add(Long.parseLong(t));
            } catch (NumberFormatException ignore) {
                // 무시(사용자가 조작한 경우 대비)
            }
        }
        return out;
    }

    // -----------------------------
    // 내부 유틸: 이미지 URL 뽑기
    // -----------------------------
    private List<String> resolveImageUrls(List<BoardPostImage> images) {
        if (images == null || images.isEmpty()) return List.of();

        List<String> result = new ArrayList<>();
        for (BoardPostImage img : images) {
            String url = resolveImageUrl(img);
            if (url != null && !url.isBlank()) result.add(url);
        }
        return result;
    }

    /**
     * BoardPostImage 엔티티 필드명이 달라져도 최대한 안전하게 url을 찾는다.
     * 1) url / imageUrl / imagePath / path / fileUrl
     * 2) storedName 계열이면 /uploads/board/ + storedName
     */
    private String resolveImageUrl(Object imageEntity) {
        try {
            BeanWrapper bw = new BeanWrapperImpl(imageEntity);

            for (String p : List.of("url", "imageUrl", "imageURL", "imagePath", "path", "fileUrl", "fileURL")) {
                if (bw.isReadableProperty(p)) {
                    String v = asString(bw.getPropertyValue(p));
                    if (v != null && !v.isBlank()) return v;
                }
            }

            for (String p : List.of("storedName", "storedFileName", "storedFilename", "savedName", "fileName", "filename")) {
                if (bw.isReadableProperty(p)) {
                    String v = asString(bw.getPropertyValue(p));
                    if (v != null && !v.isBlank()) return "/uploads/board/" + v;
                }
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    // ======================
    // Form DTO
    // ======================
    public static class BoardWriteRequest {
        @NotBlank(message = "제목을 입력하세요.")
        private String title;

        @NotBlank(message = "내용을 입력하세요.")
        private String content;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class BoardEditRequest {
        @NotBlank(message = "제목을 입력하세요.")
        private String title;

        @NotBlank(message = "내용을 입력하세요.")
        private String content;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
