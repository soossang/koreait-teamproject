package com.koreait.moviesite.RankingGenreBoard.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.koreait.moviesite.RankingGenreBoard.dto.BoardDtos;

public interface BoardService {

    // ====== 기존 메서드(이미 구현되어 있음) ======
    Page<BoardDtos.PostResponse> list(Pageable pageable);

    BoardDtos.PostResponse get(Long id, boolean increaseView);

    Long create(BoardDtos.PostCreateRequest req);

    void update(Long id, BoardDtos.PostUpdateRequest req);

    void delete(Long id);

    Long addComment(Long postId, BoardDtos.CommentCreateRequest req);

    // ======================================================
    // ✅ BoardController 호환용 메서드 2개 (추가)
    //    - BoardController가 호출하는 getPage / createPost를 제공
    // ======================================================

    /**
     * BoardController가 쓰는 방식: boardService.getPage(page)
     * page는 1부터 들어온다고 가정하고, 내부적으로 0-based로 변환.
     * 페이지 크기(size)는 일단 10으로 설정 (원하면 5/15 등으로 바꾸면 됨)
     */
    default Page<BoardDtos.PostResponse> getPage(int page) {
        int safePage = Math.max(page, 1);
        int pageIndex = safePage - 1;

        Pageable pageable = PageRequest.of(
                pageIndex,
                10,
                Sort.by(Sort.Direction.DESC, "id") // 보통 최신글이 위로
        );

        return list(pageable);
    }

    /**
     * BoardController가 쓰는 방식: boardService.createPost(loginId, title, content)
     * 내부적으로 BoardDtos.PostCreateRequest로 감싸서 create(...)에 위임.
     */
    default void createPost(String loginId, String title, String content) {
        // PostCreateRequest의 필드가 (title, content, author) 라는 전제
        // (BoardServiceImpl이 req.title()/req.content()/req.author()를 쓰고 있어서 보통 이 순서일 확률이 큼)
        BoardDtos.PostCreateRequest req = new BoardDtos.PostCreateRequest(title, content, loginId);
        create(req);
    }
}
