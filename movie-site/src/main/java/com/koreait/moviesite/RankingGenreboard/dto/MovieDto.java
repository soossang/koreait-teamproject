package com.koreait.moviesite.RankingGenreboard.dto;

public record MovieDto(
        Long id,
        String title,
        String director,
        int year,
        String genre,
        Long boxOfficeGross
) {}
