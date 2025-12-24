package com.koreait.moviesite.Common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthPageController {

    @GetMapping("/login")
    public String login() {
        return "forward:/Member/login.html";
    }

    @GetMapping("/signup")
    public String signup() {
        return "forward:/Member/signup.html";
    }

    @GetMapping("/mypage")
    public String mypage() {
        return "forward:/Member/mypage.html";
    }
}


