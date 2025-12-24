package com.koreait.moviesite.RankingGenreBoard.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.koreait.moviesite.RankingGenreBoard.dto.BoardDtos;

public interface BoardService {

    // ====== 기존 메서드 ======
    Page<BoardDtos.PostResponse> list(Pageable pageable);

    BoardDtos.PostResponse get(Long id, boolean increaseView);

    Long create(BoardDtos.PostCreateRequest req);

    void update(Long id, BoardDtos.PostUpdateRequest req);

    void delete(Long id);

    Long addComment(Long postId, BoardDtos.CommentCreateRequest req);

    // ======================================================
    // ✅ 컨트롤러 편의 메서드 (default)
    // ======================================================

    /**
     * /board?page=1 형태 페이징 지원
     */
    default Page<BoardDtos.PostResponse> getPage(int page) {
        int safePage = Math.max(page, 1);
        int pageIndex = safePage - 1;

        Pageable pageable = PageRequest.of(
                pageIndex,
                10,
                Sort.by(Sort.Direction.DESC, "id")
        );

        return list(pageable);
    }

    /**
     * ✅ 핵심 수정: void → Long
     * - 글 저장 후 postId를 리턴해야 이미지와 연결 가능
     * - 기존처럼 boardService.createPost(...) 호출만 해도 (리턴 무시) 컴파일 OK
     */
    default Long createPost(String loginId, String title, String content) {
        BoardDtos.PostCreateRequest req = new BoardDtos.PostCreateRequest(title, content, loginId);
        return create(req);
    }
}
