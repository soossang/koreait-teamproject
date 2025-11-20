package com.koreait.moviesite.controller;

import com.koreait.moviesite.dto.MovieDto;
import com.koreait.moviesite.service.MovieService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MovieController {
    private final MovieService movieService;
    public MovieController(MovieService movieService){ this.movieService = movieService; }

    @GetMapping("/movies")
    public Page<MovieDto> movies(@RequestParam(name = "genre", required = false) String genre,
                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                 @RequestParam(name = "size", defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size);
        if (genre == null || genre.isBlank()) {
            return movieService.all(pageable);       // ★ 장르 없으면 전체
        }
        return movieService.byGenre(genre, pageable);
    }
}
