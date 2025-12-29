package com.koreait.moviesite.Member.dto;

import java.time.LocalDateTime;

public record MemberCommentSummaryResponse(
        Long id,
        Long postId,
        String postTitle,
        String author,
        String content,
        LocalDateTime createdAt
) {}
