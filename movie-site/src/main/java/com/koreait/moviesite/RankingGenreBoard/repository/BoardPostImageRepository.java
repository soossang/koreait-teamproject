package com.koreait.moviesite.RankingGenreBoard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.koreait.moviesite.RankingGenreBoard.entity.BoardPostImage;

import java.util.List;

public interface BoardPostImageRepository extends JpaRepository<BoardPostImage, Long> {
    // ✅ displayOrder 우선, 같으면 id로 안정 정렬
    List<BoardPostImage> findByPostIdOrderByDisplayOrderAscIdAsc(Long postId);

    long countByPostId(Long postId);

    List<BoardPostImage> findByPostIdAndIdIn(Long postId, List<Long> ids);

    @Query("select coalesce(max(b.displayOrder), 0) from BoardPostImage b where b.postId = :postId")
    int findMaxDisplayOrderByPostId(@Param("postId") Long postId);
}
