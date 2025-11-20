package com.koreait.moviesite.service;

import com.koreait.moviesite.dao.MovieDao;
import com.koreait.moviesite.dto.MovieDto;
import com.koreait.moviesite.entity.MovieEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {
    private final MovieDao movieDao;
    public MovieServiceImpl(MovieDao movieDao) { this.movieDao = movieDao; }

    @Override
    public List<MovieDto> topBoxOffice(int limit) {
        return movieDao.findTop10ByOrderByBoxOfficeGrossDesc()
                .stream().limit(limit).map(this::toDto).toList();
    }

    @Override
    public List<String> genres() { return movieDao.findDistinctGenres(); }

    @Override
    public Page<MovieDto> byGenre(String genre, Pageable pageable) {
        return movieDao.findByGenreIgnoreCase(genre, pageable).map(this::toDto);
    }
    @Override
    public Page<MovieDto> all(Pageable pageable) {
        return movieDao.findAll(pageable).map(this::toDto);
    }
    private MovieDto toDto(MovieEntity e) {
        return new MovieDto(e.getId(), e.getTitle(), e.getDirector(), e.getYear(), e.getGenre(), e.getBoxOfficeGross());
    }
}
