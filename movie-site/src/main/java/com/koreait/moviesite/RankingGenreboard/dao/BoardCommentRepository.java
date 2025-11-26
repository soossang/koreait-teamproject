package com.koreait.moviesite.RankingGenreboard.dao;

import com.koreait.moviesite.RankingGenreboard.entity.BoardComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> { }
