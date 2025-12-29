package com.koreait.moviesite.Member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberPageController {

    // ✅ 예전 정적 주소로 접근하는 경우만 새 주소로 보내기(선택)
    @GetMapping("/Member/index.html")
    public String legacyIndex() { return "redirect:/"; }

    @GetMapping("/Member/login.html")
    public String legacyLogin() { return "redirect:/login"; }

    @GetMapping("/Member/signup.html")
    public String legacySignup() { return "redirect:/signup"; }

    @GetMapping("/Member/mypage.html")
    public String legacyMypage() { return "redirect:/mypage"; }
}
