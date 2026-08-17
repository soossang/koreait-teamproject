package com.koreait.moviesite.Member.config;

import com.koreait.moviesite.Member.dao.MemberRepository;
import com.koreait.moviesite.Member.entity.MemberEntity;
import com.koreait.moviesite.Member.entity.MemberRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 명시적으로 활성화된 환경에서만 최초 관리자 계정을 생성합니다.
 * 이미 같은 아이디의 계정이 있으면 역할이나 비밀번호를 변경하지 않습니다.
 */
@Component
@ConditionalOnProperty(name = "app.admin.bootstrap.enabled", havingValue = "true")
public class AdminAccountInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final String loginId;
    private final String rawPassword;

    public AdminAccountInitializer(MemberRepository memberRepository,
                                   PasswordEncoder passwordEncoder,
                                   @Value("${app.admin.bootstrap.login-id:}") String loginId,
                                   @Value("${app.admin.bootstrap.password:}") String rawPassword) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginId = loginId == null ? "" : loginId.trim();
        this.rawPassword = rawPassword == null ? "" : rawPassword;
    }

    @Override
    public void run(String... args) {
        if (loginId.isBlank() || rawPassword.isBlank()) {
            throw new IllegalStateException(
                    "관리자 초기화를 활성화하려면 ADMIN_BOOTSTRAP_LOGIN_ID와 ADMIN_BOOTSTRAP_PASSWORD가 필요합니다."
            );
        }

        if (memberRepository.findByLoginId(loginId).isPresent()) {
            // 보안상 기존 계정의 역할과 비밀번호는 자동으로 덮어쓰지 않는다.
            return;
        }

        MemberEntity admin = new MemberEntity();
        admin.setLoginId(loginId);
        admin.setPassword(passwordEncoder.encode(rawPassword));
        admin.setRole(MemberRole.ADMIN);
        admin.setActive(true);
        admin.setEmailVerified(false);
        admin.setPhoneVerified(false);
        memberRepository.save(admin);
    }
}
