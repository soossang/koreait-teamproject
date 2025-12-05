package com.koreait.moviesite.DetailpageReserve.service;

import com.koreait.movie_project.domain.Movie;
import com.koreait.movie_project.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> findAll() {
        return movieRepository.findAll();
    }

    public Movie findById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("영화를 찾을 수 없습니다. id=" + id));
    }

    // 테스트용 더미 데이터 넣고 싶으면 여기에 save 메서드 만들어도 됨
}
