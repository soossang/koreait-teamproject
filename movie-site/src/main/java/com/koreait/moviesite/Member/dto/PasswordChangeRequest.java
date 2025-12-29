package com.koreait.moviesite.Member.dto;

public record PasswordChangeRequest(
        String currentPassword,
        String newPassword
) {}
