package com.koreait.moviesite.RankingGenreBoard.controller;

import com.koreait.moviesite.RankingGenreBoard.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")	
public class RankingMovieController {

    private final GenreService genreService;
    
    public RankingMovieController(GenreService genreService) {
    	this.genreService = genreService;
    }

    // 예: /api/movies?genre=액션&limit=100&dir=asc
    @GetMapping("/movies")
    public ResponseEntity<List<Map<String, String>>> movies(
            @RequestParam(name = "genre") String genre,
            @RequestParam(name = "limit", defaultValue = "100") int limit,
            @RequestParam(name = "dir",   defaultValue = "asc") String dir
    ) {
        var titles = genreService.titles(genre, limit, dir);
        var body = titles.stream()
                .map(t -> Collections.singletonMap("movieNm", t))
                .toList();
        return ResponseEntity.ok(body);
    }
}
