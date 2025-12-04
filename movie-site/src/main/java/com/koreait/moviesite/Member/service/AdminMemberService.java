package com.koreait.moviesite.Member.service;

import com.koreait.moviesite.Member.dao.MemberRepository;
import com.koreait.moviesite.Member.dto.AdminUpdateMemberRequest;
import com.koreait.moviesite.Member.dto.MemberProfileResponse;
import com.koreait.moviesite.Member.entity.MemberEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminMemberService {

    private final MemberRepository memberRepository;

    public AdminMemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<MemberProfileResponse> getAllMembers() {
        return memberRepository.findAll()
                .stream()
                .map(this::toProfileResponse)
                .toList();
    }

    public MemberProfileResponse updateMember(Long targetMemberId, AdminUpdateMemberRequest request) {
        MemberEntity member = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        if (request.active() != null) member.setActive(request.active());
        if (request.role() != null) member.setRole(request.role());

        MemberEntity saved = memberRepository.save(member);
        return toProfileResponse(saved);
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
