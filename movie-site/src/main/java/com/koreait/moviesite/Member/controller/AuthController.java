package com.koreait.moviesite.Member.controller;

import com.koreait.moviesite.Member.dto.LoginRequest;
import com.koreait.moviesite.Member.dto.LoginResponse;
import com.koreait.moviesite.Member.dto.SignupRequest;
import com.koreait.moviesite.Member.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            authService.signup(request);
            return ResponseEntity.status(201).body(Map.of("message", "회원가입이 완료되었습니다."));
        } catch (IllegalArgumentException ex) {
            // 중복/유효성 에러
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (IllegalStateException ex) {
            // 충돌(예: 중복)
            return ResponseEntity.status(409).body(Map.of("message", ex.getMessage()));
        }
    }

@PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return ResponseEntity.noContent().build();
    }
}

