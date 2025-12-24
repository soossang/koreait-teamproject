package com.koreait.moviesite.Common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

    @GetMapping("/admin")
    public String admin() {
        // 정적 관리자 페이지로 forward
        return "forward:/admin/index.html";
    }
}
