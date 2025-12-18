package com.koreait.moviesite.Common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    @GetMapping("/")
    public String root() {
        // ✅ 주소는 그대로 "/" 유지하면서, 내부적으로 Member/index.html을 보여줌
        return "forward:/Member/index.html";
    }

}
