package com.koreait.moviesite.Member.dto;

import com.koreait.moviesite.Member.entity.MemberRole;

public record AdminUpdateMemberRequest(
        Boolean active,
        MemberRole role
) {}
