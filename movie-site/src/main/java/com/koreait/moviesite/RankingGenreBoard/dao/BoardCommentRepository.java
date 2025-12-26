package com.koreait.moviesite.RankingGenreBoard.dao;

import com.koreait.moviesite.RankingGenreBoard.entity.BoardComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

    // 댓글 수 (AdminBoardApiController에서 사용)
    long countByPost_Id(Long postId);

    // 게시글 삭제 시 FK 방지용 (BoardServiceImpl/AdminBoardApiController에서 사용)
    void deleteByPost_Id(Long postId);

    // 댓글 목록
    List<BoardComment> findByPost_IdOrderByIdAsc(Long postId);
}
