package com.koreait.moviesite.RankingGenreBoard.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.koreait.moviesite.RankingGenreBoard.dto.BoardDtos;

public interface BoardService {
    Page<BoardDtos.PostResponse> list(Pageable pageable);
    BoardDtos.PostResponse get(Long id, boolean increaseView);
    Long create(BoardDtos.PostCreateRequest req);
    void update(Long id, BoardDtos.PostUpdateRequest req);
    void delete(Long id);
    Long addComment(Long postId, BoardDtos.CommentCreateRequest req);
}
