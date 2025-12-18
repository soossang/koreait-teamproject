package com.koreait.moviesite.RankingGenreBoard.repository;

import com.koreait.moviesite.RankingGenreBoard.entity.BoardPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {
}
