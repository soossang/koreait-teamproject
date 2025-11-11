package com.koreait.moviesite.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        // localhost:9090 접속 시 /api/movies 로 이동
        return "redirect:/api/movies";
    }
}
