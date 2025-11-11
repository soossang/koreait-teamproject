package com.koreait.moviesite.service;

import com.koreait.moviesite.dao.MovieDao;
import com.koreait.moviesite.dto.MovieDto;
import com.koreait.moviesite.entity.MovieEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieDao movieDao;

    public MovieServiceImpl(MovieDao movieDao) {
        this.movieDao = movieDao;
    }

    @Override
    public List<MovieDto> getAllMovies() {
        return movieDao.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public MovieDto getMovieById(Long id) {
        MovieEntity entity = movieDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 영화가 없습니다: " + id));
        return toDto(entity);
    }

    @Override
    public MovieDto createMovie(MovieDto movieDto) {
        MovieEntity saved = movieDao.save(toEntity(movieDto));
        return toDto(saved);
    }

    // ===== 매핑 메소드 =====
    private MovieDto toDto(MovieEntity e) {
        return new MovieDto(
                e.getId(),
                e.getTitle(),
                e.getDirector(),
                e.getYear(),
                e.getGenre()
        );
    }

    private MovieEntity toEntity(MovieDto d) {
        return new MovieEntity(
                d.id(),
                d.title(),
                d.director(),
                d.year(),
                d.genre()
        );
    }
}
