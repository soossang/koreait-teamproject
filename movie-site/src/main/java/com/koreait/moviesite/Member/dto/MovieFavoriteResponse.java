package com.koreait.moviesite.Member.dto;

import java.time.LocalDateTime;

public record MovieFavoriteResponse(
        Long movieId,
        String title,
        String director,
        int year,
        String genre,
        Long boxOfficeGross,
        LocalDateTime createdAt
) {}
