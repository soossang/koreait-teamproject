package com.koreait.moviesite.Member.security;

import com.koreait.moviesite.Member.entity.MemberRole;

public record AuthenticatedMember(
        Long id,
        String loginId,
        MemberRole role
) {}
