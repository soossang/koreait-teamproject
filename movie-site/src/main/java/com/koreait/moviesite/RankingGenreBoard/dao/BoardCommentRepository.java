package com.koreait.moviesite.RankingGenreBoard.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.koreait.moviesite.RankingGenreBoard.entity.BoardComment;

@Repository
public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> { }
