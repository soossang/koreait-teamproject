package com.koreait.moviesite.Member.dto;

import java.time.LocalDate;

public record MemberUpdateRequest(
        String name,
        String nickname,
        LocalDate birth,
        String address,
        String profileImageUrl
) {}
