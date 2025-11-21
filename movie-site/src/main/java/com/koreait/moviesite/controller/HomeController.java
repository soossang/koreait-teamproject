package com.koreait.moviesite.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {      // 루트 접속 시 index.html
        return "index";
    }

    @GetMapping("/board")
    public String board() {     // /board 접속 시 board.html
        return "board";
    }
}
