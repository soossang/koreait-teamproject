package com.koreait.moviesite.Common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthPageController {

    @GetMapping("/login")
    public String login() {
        return "Member/login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "Member/signup";
    }

    @GetMapping("/mypage")
    public String mypage() {
        return "Member/mypage";
    }
}

<<<<<<< HEAD

=======
>>>>>>> branch 'practice' of https://github.com/soossang/koreait-teamproject.git
