package com.koreait.moviesite.controller;

import com.koreait.moviesite.dto.PasswordChangeRequest;
import com.koreait.moviesite.dto.UserProfileResponse;
import com.koreait.moviesite.dto.UserUpdateRequest;
import com.koreait.moviesite.security.AuthenticatedUser;
import com.koreait.moviesite.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 로그인한 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMe(
            @RequestAttribute("authUser") AuthenticatedUser authUser
    ) {
        UserProfileResponse profile = userService.getProfile(authUser.id());
        return ResponseEntity.ok(profile);
    }

    // 내 정보 수정
    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMe(
            @RequestAttribute("authUser") AuthenticatedUser authUser,
            @RequestBody UserUpdateRequest request
    ) {
        UserProfileResponse profile = userService.updateProfile(authUser.id(), request);
        return ResponseEntity.ok(profile);
    }

    // 비밀번호 변경
    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @RequestAttribute("authUser") AuthenticatedUser authUser,
            @RequestBody PasswordChangeRequest request
    ) {
        userService.changePassword(authUser.id(), request);
        return ResponseEntity.ok().build();
    }
}
