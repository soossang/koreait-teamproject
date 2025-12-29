package com.koreait.moviesite.RankingGenreBoard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GenreController {

    // /genre 진입 시 템플릿: templates/RankingGenreboard/genre.html 렌더
    @GetMapping("/genre")
    public String genrePage() {
        return "RankingGenreboard/genre";
    }
}
	