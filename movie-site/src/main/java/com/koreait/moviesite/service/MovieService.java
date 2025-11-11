package com.koreait.moviesite.service;

import com.koreait.moviesite.dto.MovieDto;

import java.util.List;

public interface MovieService {
    List<MovieDto> getAllMovies();
    MovieDto getMovieById(Long id);
    MovieDto createMovie(MovieDto movieDto);
}
