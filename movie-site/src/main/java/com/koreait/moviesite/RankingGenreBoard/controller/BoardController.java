package com.koreait.moviesite.RankingGenreBoard.controller;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.koreait.moviesite.RankingGenreBoard.dto.BoardDtos;
import com.koreait.moviesite.RankingGenreBoard.dto.BoardWriteRequest;
import com.koreait.moviesite.RankingGenreBoard.entity.BoardPostImage;
import com.koreait.moviesite.RankingGenreBoard.service.BoardImageService;
import com.koreait.moviesite.RankingGenreBoard.service.BoardService;

@Controller
@RequestMapping("/board")
public class BoardController {

    private static final String VIEW_BASE = "RankingGenreboard";

    private final BoardService boardService;
    private final BoardImageService boardImageService;

    public BoardController(BoardService boardService, BoardImageService boardImageService) {
        this.boardService = boardService;
        this.boardImageService = boardImageService;
    }

    // ✅ 목록
    @GetMapping
    public String list(@RequestParam(name = "page", defaultValue = "1") int page, Model model) {

        int safePage = Math.max(page, 1);
        int size = 10;

        Page<BoardDtos.PostResponse> postsPage = boardService.list(
                PageRequest.of(safePage - 1, size, Sort.by(Sort.Direction.DESC, "id"))
        );

        // 템플릿 호환용(여러 버전 대비)
        model.addAttribute("page", postsPage);

        model.addAttribute("posts", postsPage.getContent());
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", postsPage.getTotalPages());
        model.addAttribute("totalElements", postsPage.getTotalElements());

        return VIEW_BASE + "/board";
    }

    // ✅ 상세
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id,
                         @RequestParam(name = "page", defaultValue = "1") int backPage,
                         Model model) {

        BoardDtos.PostResponse post = boardService.get(id, true);

        List<BoardPostImage> images = boardImageService.listByPostId(id);
        List<String> imageUrls = resolveImageUrls(images);

        model.addAttribute("post", post);

        // boarddetail.html이 images를 쓰는 버전이면 그대로 표시됨 :contentReference[oaicite:4]{index=4}
        model.addAttribute("images", images);

        // 혹시 엔티티 필드명이 달라도 출력되도록 안전장치로 같이 내려줌
        model.addAttribute("imageUrls", imageUrls);

        model.addAttribute("backPage", backPage);

        return VIEW_BASE + "/boarddetail";
    }

    // ✅ 글쓰기 화면
    @GetMapping("/write")
    public String writeForm(Model model, HttpSession session) {
        String loginId = (String) session.getAttribute("loginId");
        if (loginId == null || loginId.isBlank()) {
            return "redirect:/login?redirect=/board/write";
        }
        model.addAttribute("form", new BoardWriteRequest());
        return VIEW_BASE + "/write";
    }

    // ✅ 글쓰기 저장 (+ 이미지 업로드)
    @PostMapping("/write")
    public String writeSubmit(@ModelAttribute("form") BoardWriteRequest form,
                              @RequestParam(value = "images", required = false) List<MultipartFile> images,
                              HttpSession session) {

        String loginId = (String) session.getAttribute("loginId");
        if (loginId == null || loginId.isBlank()) {
            return "redirect:/login?redirect=/board/write";
        }

        // ✅ 글 저장 후 postId 확보 (이미지 연결하려면 필수)
        Long postId = boardService.create(new BoardDtos.PostCreateRequest(
                form.getTitle(),
                form.getContent(),
                loginId
        ));

        // ✅ 이미지 저장
        if (images != null && images.stream().anyMatch(f -> f != null && !f.isEmpty())) {
            boardImageService.saveImages(postId, images);
        }

        return "redirect:/board/" + postId + "?page=1";
    }

    // -----------------------------
    // 이미지 URL 추출(엔티티 필드명 달라도 최대한 대응)
    // -----------------------------
    private List<String> resolveImageUrls(List<BoardPostImage> images) {
        if (images == null || images.isEmpty()) return List.of();

        List<String> result = new ArrayList<>();
        for (BoardPostImage img : images) {
            String url = resolveImageUrl(img);
            if (url != null && !url.isBlank()) {
                result.add(url);
            }
        }
        return result;
    }

    private String resolveImageUrl(Object imageEntity) {
        try {
            BeanWrapper bw = new BeanWrapperImpl(imageEntity);

            // url 계열 우선
            for (String p : List.of("url", "imageUrl", "imagePath", "path", "fileUrl")) {
                if (bw.isReadableProperty(p)) {
                    String v = asString(bw.getPropertyValue(p));
                    if (v != null && !v.isBlank()) return v.startsWith("/") ? v : "/" + v;
                }
            }

            // 파일명 계열이면 /uploads/board/ prefix
            for (String p : List.of("storedName", "storedFileName", "savedName", "fileName")) {
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
}
