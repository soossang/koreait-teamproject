package com.koreait.moviesite.Member.controller;

import com.koreait.moviesite.Member.dto.LoginRequest;
import com.koreait.moviesite.Member.dto.LoginResponse;
import com.koreait.moviesite.Member.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest req,
                                               HttpServletRequest request) {

        LoginResponse res = authService.login(req);

        HttpSession session = request.getSession(true);
        session.setAttribute("loginId", res.getLoginId());
        session.setAttribute("role", res.getRole());

        // ✅ 여기 핵심: Authorization 헤더 추가
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, res.getTokenType() + " " + res.getToken())
                .body(res);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return ResponseEntity.noContent().build();
    }
}

