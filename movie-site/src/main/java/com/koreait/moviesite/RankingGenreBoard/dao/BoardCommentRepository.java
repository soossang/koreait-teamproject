package com.koreait.moviesite.RankingGenreBoard.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.koreait.moviesite.RankingGenreBoard.entity.BoardComment;

@Repository
public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

    // 댓글 목록(게시글 기준)
    List<BoardComment> findByPost_IdOrderByIdAsc(Long postId);

    // 게시글 삭제 시 FK 오류 방지용(댓글 먼저 삭제)
    long deleteByPost_Id(Long postId);
}
