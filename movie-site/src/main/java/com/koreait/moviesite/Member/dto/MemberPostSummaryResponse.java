package com.koreait.moviesite.Member.dto;

import java.time.LocalDateTime;

public record MemberPostSummaryResponse(
        Long id,
        String title,
        String author,
        long viewCount,
        LocalDateTime createdAt
) {}
