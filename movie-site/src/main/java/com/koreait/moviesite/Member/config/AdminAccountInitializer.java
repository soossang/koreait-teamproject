package com.koreait.moviesite.Member.config;

import com.koreait.moviesite.Member.dao.MemberRepository;
import com.koreait.moviesite.Member.entity.MemberEntity;
import com.koreait.moviesite.Member.entity.MemberRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 기본 관리자 계정을 자동 생성/보정합니다.
 * - loginId: admin
 * - password: admin1234
 *
 * NOTE:
 * 기존 DB에 평문/다른 인코딩 비밀번호가 섞여 있으면 BCrypt.matches()에서 예외가 날 수 있어서,
 * 팀프로젝트에서는 "항상 admin1234로 재설정"하는 방식으로 안전하게 보정합니다.
 */
@Component
public class AdminAccountInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountInitializer(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        final String loginId = "admin";
        final String rawPassword = "admin1234";

        memberRepository.findByLoginId(loginId).ifPresentOrElse(existing -> {
            boolean changed = false;

            if (existing.getRole() != MemberRole.ADMIN) {
                existing.setRole(MemberRole.ADMIN);
                changed = true;
            }
            if (!existing.isActive()) {
                existing.setActive(true);
                changed = true;
            }

            // 항상 비밀번호를 admin1234로 재설정 (인코딩 형식이 무엇이든 안전)
            existing.setPassword(passwordEncoder.encode(rawPassword));
            changed = true;

            if (changed) {
                memberRepository.save(existing);
            }
        }, () -> {
            MemberEntity admin = new MemberEntity();
            admin.setLoginId(loginId);
            admin.setPassword(passwordEncoder.encode(rawPassword));
            admin.setRole(MemberRole.ADMIN);
            admin.setActive(true);
            admin.setEmailVerified(false);
            admin.setPhoneVerified(false);
            memberRepository.save(admin);
        });
    }
}
