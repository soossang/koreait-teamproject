package com.koreait.moviesite.Member.dto;

import java.time.LocalDate;

public record MovieRatingRequest(
        int rating,
        String review,
        LocalDate watchedAt
) {}
