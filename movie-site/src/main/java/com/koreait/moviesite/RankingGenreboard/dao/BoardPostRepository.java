package com.koreait.moviesite.RankingGenreboard.dao;

import com.koreait.moviesite.RankingGenreboard.entity.BoardPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardPostRepository extends JpaRepository<BoardPost, Long> { }
