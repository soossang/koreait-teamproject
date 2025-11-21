package com.koreait.moviesite.controller;

import com.koreait.moviesite.dto.AdminUpdateUserRequest;
import com.koreait.moviesite.dto.UserProfileResponse;
import com.koreait.moviesite.entity.Role;
import com.koreait.moviesite.security.AuthenticatedUser;
import com.koreait.moviesite.service.AdminUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<List<UserProfileResponse>> getAllUsers(
            @RequestAttribute("authUser") AuthenticatedUser authUser
    ) {
        if (authUser.role() != Role.ADMIN) {
            return ResponseEntity.status(403).build();
        }
        List<UserProfileResponse> users = adminUserService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserProfileResponse> updateUser(
            @RequestAttribute("authUser") AuthenticatedUser authUser,
            @PathVariable Long id,
            @RequestBody AdminUpdateUserRequest request
    ) {
        if (authUser.role() != Role.ADMIN) {
            return ResponseEntity.status(403).build();
        }
        UserProfileResponse updated = adminUserService.updateUser(id, request);
        return ResponseEntity.ok(updated);
    }
}
