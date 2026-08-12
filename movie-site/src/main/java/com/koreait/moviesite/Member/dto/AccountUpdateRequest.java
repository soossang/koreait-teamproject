package com.koreait.moviesite.Member.dto;

public record AccountUpdateRequest(
        String currentPassword,
        String newPassword,
        String newPasswordConfirm,
        String email,
        String phone
) {}
