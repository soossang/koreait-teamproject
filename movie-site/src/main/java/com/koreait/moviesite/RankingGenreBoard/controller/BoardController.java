package com.koreait.moviesite.RankingGenreBoard.controller;

import com.koreait.moviesite.RankingGenreBoard.dto.BoardWriteForm;
import com.koreait.moviesite.RankingGenreBoard.entity.BoardPost;
import com.koreait.moviesite.RankingGenreBoard.repository.BoardPostRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/board")
public class BoardController {

    private final BoardPostRepository boardPostRepository;

    public BoardController(BoardPostRepository boardPostRepository) {
        this.boardPostRepository = boardPostRepository;
    }

    // 게시판 목록
    @GetMapping
    public String board(Model model) {
        List<BoardPost> posts = boardPostRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("posts", posts);
        return "board/board"; // templates/board/board.html
    }

    // 글쓰기 폼
    @GetMapping("/write")
    public String writeForm(HttpServletRequest request, Model model) {
        String loginId = getLoginId(request);
        if (loginId == null) {
            // 로그인 안 했으면 로그인 페이지로 보내기(원래 가려던 곳도 같이)
            return "redirect:/login?redirect=/board/write";
        }

        model.addAttribute("form", new BoardWriteForm());
        model.addAttribute("loginId", loginId);
        return "board/write"; // templates/board/write.html
    }

    // 글 저장
    @PostMapping("/write")
    public String writeSubmit(@Valid @ModelAttribute("form") BoardWriteForm form,
                              BindingResult bindingResult,
                              HttpServletRequest request) {

        String loginId = getLoginId(request);
        if (loginId == null) {
            return "redirect:/login?redirect=/board/write";
        }

        if (bindingResult.hasErrors()) {
            return "board/write";
        }

        BoardPost post = new BoardPost();
        post.setAuthor(loginId);
        post.setTitle(form.getTitle());
        post.setContent(form.getContent());
        boardPostRepository.save(post);

        return "redirect:/board";
    }

    // ✅ 로그인 아이디 찾기(세션 or Spring Security)
    private String getLoginId(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            return principal.getName(); // Spring Security 사용 시
        }

        HttpSession session = request.getSession(false);
        if (session == null) return null;

        Object loginId = session.getAttribute("loginId"); // 네가 로그인 성공 시 넣어둔 키로 맞추기
        return (loginId == null) ? null : loginId.toString();
    }
}
