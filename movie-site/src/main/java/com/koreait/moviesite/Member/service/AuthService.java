package com.koreait.moviesite.Member.service;

import com.koreait.moviesite.Member.dao.MemberRepository;
import com.koreait.moviesite.Member.dto.LoginRequest;
import com.koreait.moviesite.Member.dto.LoginResponse;
import com.koreait.moviesite.Member.dto.SignupRequest;
import com.koreait.moviesite.Member.entity.MemberEntity;
import com.koreait.moviesite.Member.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(MemberRepository memberRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public void signup(SignupRequest request) {
        if (memberRepository.existsByLoginId(request.loginId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if ((request.email() == null || request.email().isBlank()) &&
                (request.phone() == null || request.phone().isBlank())) {
            throw new IllegalArgumentException("이메일 또는 휴대폰 번호 중 하나는 반드시 입력해야 합니다.");
        }

        if (request.email() != null && !request.email().isBlank()
                && memberRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (request.phone() != null && !request.phone().isBlank()
                && memberRepository.existsByPhone(request.phone())) {
            throw new IllegalArgumentException("이미 사용 중인 전화번호입니다.");
        }

        MemberEntity member = new MemberEntity();
        member.setLoginId(request.loginId());
        member.setPassword(passwordEncoder.encode(request.password()));
        member.setEmail(request.email());
        member.setPhone(request.phone());

        memberRepository.save(member);
    }

    public LoginResponse login(LoginRequest request) {
        MemberEntity member = memberRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!member.isActive()) {
            throw new IllegalStateException("비활성화된 계정입니다.");
        }

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String token = jwtTokenProvider.generateToken(member);
        long expiresIn = jwtTokenProvider.getValidityInSeconds();

        return new LoginResponse(token, "Bearer", expiresIn);
    }
}
