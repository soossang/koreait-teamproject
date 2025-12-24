package com.koreait.moviesite.RankingGenreBoard.dao;

import com.koreait.moviesite.RankingGenreBoard.entity.BoardComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

    long countByPost_Id(Long postId);

    void deleteByPost_Id(Long postId);
}
