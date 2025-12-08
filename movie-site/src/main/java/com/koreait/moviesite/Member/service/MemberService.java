package com.koreait.moviesite.Member.service;

import com.koreait.moviesite.Member.dao.MemberRepository;
import com.koreait.moviesite.Member.dto.MemberProfileResponse;
import com.koreait.moviesite.Member.dto.MemberUpdateRequest;
import com.koreait.moviesite.Member.dto.PasswordChangeRequest;
import com.koreait.moviesite.Member.entity.MemberEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository,
                         PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
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
