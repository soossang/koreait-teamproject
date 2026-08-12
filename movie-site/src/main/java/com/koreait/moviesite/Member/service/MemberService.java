package com.koreait.moviesite.Member.service;

import com.koreait.moviesite.Member.dao.MemberRepository;
import com.koreait.moviesite.Member.dto.AccountUpdateRequest;
import com.koreait.moviesite.Member.dto.MemberProfileResponse;
import com.koreait.moviesite.Member.dto.MemberUpdateRequest;
import com.koreait.moviesite.Member.dto.PasswordChangeRequest;
import com.koreait.moviesite.Member.entity.MemberEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository,
                         PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ✅ 프로필 조회 메서드 추가
    public MemberProfileResponse getProfile(Long memberId) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        return toProfileResponse(member);
    }

    public MemberProfileResponse updateProfile(Long memberId, MemberUpdateRequest request) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        if (request.name() != null) member.setName(request.name());
        if (request.nickname() != null) member.setNickname(request.nickname());
        if (request.birth() != null) member.setBirth(request.birth());
        if (request.address() != null) member.setAddress(request.address());
        if (request.profileImageUrl() != null) member.setProfileImageUrl(request.profileImageUrl());

        MemberEntity saved = memberRepository.save(member);
        return toProfileResponse(saved);
    }

    public void changePassword(Long memberId, PasswordChangeRequest request) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        member.setPassword(passwordEncoder.encode(request.newPassword()));
        memberRepository.save(member);
    }

    @Transactional
    public MemberProfileResponse updateAccount(Long memberId, AccountUpdateRequest request) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        if (request.currentPassword() == null || request.currentPassword().isBlank()) {
            throw new IllegalArgumentException("현재 비밀번호를 입력해주세요.");
        }
        if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        String email = normalizeEmail(request.email());
        String phone = normalizePhone(request.phone());
        validateContact(memberId, email, phone);
        updatePasswordIfRequested(member, request);

        if (!Objects.equals(member.getEmail(), email)) {
            member.setEmail(email);
            member.setEmailVerified(false);
        }
        if (!Objects.equals(member.getPhone(), phone)) {
            member.setPhone(phone);
            member.setPhoneVerified(false);
        }

        return toProfileResponse(memberRepository.save(member));
    }

    private void validateContact(Long memberId, String email, String phone) {
        if (email == null && phone == null) {
            throw new IllegalArgumentException("이메일 또는 휴대폰 번호 중 하나는 반드시 입력해야 합니다.");
        }
        if (email != null && !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("올바른 이메일 형식을 입력해주세요.");
        }
        if (phone != null && !phone.matches("^\\d{10,11}$")) {
            throw new IllegalArgumentException("휴대폰 번호는 숫자 10~11자리로 입력해주세요.");
        }
        if (email != null && memberRepository.existsByEmailAndIdNot(email, memberId)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (phone != null && memberRepository.existsByPhoneAndIdNot(phone, memberId)) {
            throw new IllegalArgumentException("이미 사용 중인 휴대폰 번호입니다.");
        }
    }

    private void updatePasswordIfRequested(MemberEntity member, AccountUpdateRequest request) {
        String newPassword = request.newPassword();
        if (newPassword != null && !newPassword.isBlank()) {
            if (newPassword.length() < 4) {
                throw new IllegalArgumentException("새 비밀번호는 4자 이상으로 입력해주세요.");
            }
            if (!newPassword.equals(request.newPasswordConfirm())) {
                throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            }
            member.setPassword(passwordEncoder.encode(newPassword));
        } else if (request.newPasswordConfirm() != null && !request.newPasswordConfirm().isBlank()) {
            throw new IllegalArgumentException("새 비밀번호를 입력해주세요.");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) return null;
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) return null;
        return phone.replaceAll("[\\s-]", "");
    }

    private MemberProfileResponse toProfileResponse(MemberEntity member) {
        return new MemberProfileResponse(
                member.getId(),
                member.getLoginId(),
                member.getEmail(),
                member.isEmailVerified(),
                member.getPhone(),
                member.isPhoneVerified(),
                member.getName(),
                member.getNickname(),
                member.getBirth(),
                member.getAddress(),
                member.getProfileImageUrl(),
                member.getRole(),
                member.isActive(),
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }
}
