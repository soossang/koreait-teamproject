package com.koreait.moviesite.DetailpageReserve.repository;

import com.koreait.movie_project.domain.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}
