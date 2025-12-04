package com.koreait.moviesite.Member.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
