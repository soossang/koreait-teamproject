package com.koreait.moviesite.RankingGenreBoard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        // templates/RankingGenreboard/index.html
        return "RankingGenreboard/index";
    }

    @GetMapping("/board")
    public String board() {
        // templates/RankingGenreboard/board.html
        return "RankingGenreboard/board";
    }
}
