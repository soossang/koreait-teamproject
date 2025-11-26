package com.koreait.moviesite.RankingGenreboard.dao;

import com.koreait.moviesite.RankingGenreboard.entity.MovieEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieDao extends JpaRepository<MovieEntity, Long> {
    Page<MovieEntity> findByGenreIgnoreCase(String genre, Pageable pageable);
    List<MovieEntity> findTop10ByOrderByBoxOfficeGrossDesc();

    @Query("select distinct m.genre from MovieEntity m where m.genre is not null")
    List<String> findDistinctGenres();
}
