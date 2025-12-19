package com.koreait.moviesite.RankingGenreBoard.controller;

import com.koreait.moviesite.RankingGenreBoard.dto.BoardDtos;
import com.koreait.moviesite.RankingGenreBoard.dto.BoardWriteRequest;
import com.koreait.moviesite.RankingGenreBoard.service.BoardService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/board")
public class BoardController {

    // ✅ 템플릿 폴더명과 일치 (네가 알려준 위치 기준)
    private static final String VIEW_BASE = "RankingGenreboard";

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    // ✅ 게시판 목록
    @GetMapping
    public String list(@RequestParam(name = "page", defaultValue = "1") int page, Model model) {

        int safePage = Math.max(page, 1);
        int pageIndex = safePage - 1;

        Pageable pageable = PageRequest.of(pageIndex, 10, Sort.by(Sort.Direction.DESC, "id"));

        // 1) 서비스에서 Page 형태로 받기 (이미 구현되어 있음)
        Page<BoardDtos.PostResponse> pageResult = boardService.list(pageable);

        // 2) ✅ board.html이 기대하는 모델명으로 맞춰서 내려주기
        model.addAttribute("posts", pageResult.getContent());
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalElements", pageResult.getTotalElements());
        model.addAttribute("currentPage", safePage);

        return VIEW_BASE + "/board";
    }

    // ✅ 게시글 상세 (boarddetail.html과 연결)
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id,
                         @RequestParam(name = "page", defaultValue = "1") int page,
                         Model model) {

        // 조회수 증가 true
        BoardDtos.PostResponse post = boardService.get(id, true);

        model.addAttribute("post", post);
        model.addAttribute("backPage", Math.max(page, 1)); // boarddetail.html에서 목록 복귀에 사용

        return VIEW_BASE + "/boarddetail";
    }

    // ✅ 글쓰기 폼
    @GetMapping("/write")
    public String writeForm(HttpServletRequest request, Model model) {

        String loginId = getLoginIdFromSession(request);
        if (loginId == null) {
            return redirectToLoginWithRedirectParam("/board/write");
        }

        model.addAttribute("loginId", loginId);
        model.addAttribute("form", new BoardWriteRequest());

        return VIEW_BASE + "/write";
    }

    // ✅ 글쓰기 저장
    @PostMapping("/write")
    public String writeSubmit(@ModelAttribute("form") BoardWriteRequest form,
                              HttpServletRequest request) {

        String loginId = getLoginIdFromSession(request);
        if (loginId == null) {
            return redirectToLoginWithRedirectParam("/board/write");
        }

        BoardDtos.PostCreateRequest req =
                new BoardDtos.PostCreateRequest(form.getTitle(), form.getContent(), loginId);

        boardService.create(req);

        return "redirect:/board";
    }

    // -------------------
    // 세션 로그인 체크
    // -------------------
    private String getLoginIdFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;

        Object v = session.getAttribute("loginId");
        return (v == null) ? null : v.toString();
    }

    private String redirectToLoginWithRedirectParam(String redirectPath) {
        String encoded = UriUtils.encode(redirectPath, StandardCharsets.UTF_8);
        return "redirect:/login?redirect=" + encoded;
    }
}
