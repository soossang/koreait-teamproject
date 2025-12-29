package com.koreait.moviesite.Member.dto;

public record SignupRequest(
        String loginId,
        String password,
        String email,
        String phone
) {}
