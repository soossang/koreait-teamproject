package com.koreait.moviesite.RankingGenreBoard.repository;

import com.koreait.moviesite.RankingGenreBoard.entity.BoardPostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardPostImageRepository extends JpaRepository<BoardPostImage, Long> {
    List<BoardPostImage> findByPostIdOrderByIdAsc(Long postId);
}
