package com.koreait.moviesite.Member.dto;

import com.koreait.moviesite.Member.entity.MemberRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MemberProfileResponse(
        Long id,
        String loginId,
        String email,
        boolean emailVerified,
        String phone,
        boolean phoneVerified,
        String name,
        String nickname,
        LocalDate birth,
        String address,
        String profileImageUrl,
        MemberRole role,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
