package com.koreait.moviesite.RankingGenreBoard.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import com.koreait.moviesite.RankingGenreBoard.dto.BoardDtos;
import com.koreait.moviesite.RankingGenreBoard.service.BoardService;

@RestController
@RequestMapping("/api/board")
public class BoardController {
    private final BoardService service;
    public BoardController(BoardService service){ this.service = service; }

    @GetMapping("/posts/{id}")
    public BoardDtos.PostResponse get(@PathVariable("id") Long id,
                                      @RequestParam(name = "increaseView", defaultValue = "true") boolean increaseView) {
        return service.get(id, increaseView);
    }

    @GetMapping("/posts")
    public Page<BoardDtos.PostResponse> list(@RequestParam(name = "page", defaultValue = "0") int page,
                                             @RequestParam(name = "size", defaultValue = "20") int size) {
        return service.list(PageRequest.of(page, size));
    }


    @PostMapping("/posts")
    public Long create(@RequestBody @Valid BoardDtos.PostCreateRequest req) {
        return service.create(req);
    }

    @PutMapping("/posts/{id}")
    public void update(@PathVariable Long id, @RequestBody @Valid BoardDtos.PostUpdateRequest req) {
        service.update(id, req);
    }

    @DeleteMapping("/posts/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }

    @PostMapping("/posts/{id}/comments")
    public Long addComment(@PathVariable Long id, @RequestBody @Valid BoardDtos.CommentCreateRequest req) {
        return service.addComment(id, req);
    }
}
