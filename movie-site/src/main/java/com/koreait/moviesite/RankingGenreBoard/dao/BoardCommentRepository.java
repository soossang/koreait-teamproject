package com.koreait.moviesite.RankingGenreBoard.dao;

<<<<<<< HEAD
import com.koreait.moviesite.RankingGenreBoard.entity.BoardComment;
=======
import java.util.List;

>>>>>>> branch 'practice' of https://github.com/soossang/koreait-teamproject.git
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

<<<<<<< HEAD
    long countByPost_Id(Long postId);

    void deleteByPost_Id(Long postId);
=======
    // 댓글 목록(게시글 기준)
    List<BoardComment> findByPost_IdOrderByIdAsc(Long postId);

    // 게시글 삭제 시 FK 오류 방지용(댓글 먼저 삭제)
    long deleteByPost_Id(Long postId);
>>>>>>> branch 'practice' of https://github.com/soossang/koreait-teamproject.git
}
