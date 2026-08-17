package com.koreait.moviesite.Member.service;

import com.koreait.moviesite.Member.dao.MemberRepository;
import com.koreait.moviesite.Member.dto.LoginRequest;
import com.koreait.moviesite.Member.dto.LoginResponse;
import com.koreait.moviesite.Member.dto.SignupRequest;
import com.koreait.moviesite.Member.entity.MemberEntity;
import com.koreait.moviesite.Member.entity.MemberRole;
import com.koreait.moviesite.Member.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final String adminRegistrationCode;

    public AuthService(MemberRepository memberRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       @Value("${app.admin.registration-code:}") String adminRegistrationCode) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.adminRegistrationCode = adminRegistrationCode;
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

        MemberRole signupRole = resolveSignupRole(request.adminCode());

        MemberEntity member = new MemberEntity();
        member.setLoginId(request.loginId());
        member.setPassword(passwordEncoder.encode(request.password()));
        member.setEmail(request.email());
        member.setPhone(request.phone());
        member.setRole(signupRole);

        memberRepository.save(member);
    }

    private MemberRole resolveSignupRole(String adminCode) {
        if (adminCode == null || adminCode.isBlank()) {
            return MemberRole.USER;
        }

        if (adminRegistrationCode == null || adminRegistrationCode.isBlank()) {
            throw new IllegalArgumentException("관리자 회원가입이 비활성화되어 있습니다.");
        }

        if (!adminRegistrationCode.equals(adminCode.trim())) {
            throw new IllegalArgumentException("관리자 등록 코드가 올바르지 않습니다.");
        }

        return MemberRole.ADMIN;
    }

    /**
     * ✅ “아이디/비번 검증 후 MemberEntity 반환”
     * - 세션 저장, 권한(role) 확인 같은 데서 MemberEntity가 필요할 때 사용
     */
    public MemberEntity authenticate(String loginId, String password) {
        MemberEntity member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!member.isActive()) {
            throw new IllegalStateException("비활성화된 계정입니다.");
        }

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        return member;
    }

    /**
     * ✅ 기존 API용 로그인(토큰만 내려주는 형태)도 유지
     */
    public LoginResponse login(LoginRequest request) {

        MemberEntity member = memberRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!member.isActive()) {
            throw new IllegalStateException("비활성화된 계정입니다.");
        }

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        return createLoginResponse(member);
    }

    public LoginResponse issueTokenForSession(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            throw new IllegalArgumentException("로그인 세션이 없습니다.");
        }

        MemberEntity member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        if (!member.isActive()) {
            throw new IllegalStateException("비활성화된 계정입니다.");
        }

        return createLoginResponse(member);
    }

    private LoginResponse createLoginResponse(MemberEntity member) {
        String token = jwtTokenProvider.generateToken(member);
        long expiresIn = jwtTokenProvider.getValidityInSeconds();

        return new LoginResponse(
                "Bearer",
                token,
                expiresIn,
                member.getLoginId(),
                member.getRole().name()
        );
    }

}
