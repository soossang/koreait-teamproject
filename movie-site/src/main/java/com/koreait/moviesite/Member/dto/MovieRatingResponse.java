package com.koreait.moviesite.Member.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MovieRatingResponse(
        Long movieId,
        String title,
        int rating,
        String review,
        LocalDate watchedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
