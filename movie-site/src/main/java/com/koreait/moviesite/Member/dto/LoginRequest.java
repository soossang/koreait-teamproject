package com.koreait.moviesite.Member.dto;

public record LoginRequest(
        String loginId,
        String password
) {}
