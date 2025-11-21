package com.koreait.moviesite.service;

import com.koreait.moviesite.dao.UserRepository;
import com.koreait.moviesite.dto.LoginRequest;
import com.koreait.moviesite.dto.LoginResponse;
import com.koreait.moviesite.dto.SignupRequest;
import com.koreait.moviesite.entity.UserEntity;
import com.koreait.moviesite.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public void signup(SignupRequest request) {
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if ((request.email() == null || request.email().isBlank()) &&
            (request.phone() == null || request.phone().isBlank())) {
            throw new IllegalArgumentException("이메일 또는 핸드폰 번호 중 하나는 반드시 입력해야 합니다.");
        }

        if (request.email() != null && !request.email().isBlank()
                && userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (request.phone() != null && !request.phone().isBlank()
                && userRepository.existsByPhone(request.phone())) {
            throw new IllegalArgumentException("이미 사용 중인 전화번호입니다.");
        }

        UserEntity user = new UserEntity();
        user.setLoginId(request.loginId());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        user.setPhone(request.phone());

        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!user.isActive()) {
            throw new IllegalStateException("비활성화된 계정입니다.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String token = jwtTokenProvider.generateToken(user);
        long expiresIn = jwtTokenProvider.getValidityInSeconds();

        return new LoginResponse(token, "Bearer", expiresIn);
    }
}
