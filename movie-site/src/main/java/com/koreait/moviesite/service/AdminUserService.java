package com.koreait.moviesite.service;

import com.koreait.moviesite.dao.UserRepository;
import com.koreait.moviesite.dto.AdminUpdateUserRequest;
import com.koreait.moviesite.dto.UserProfileResponse;
import com.koreait.moviesite.entity.UserEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toProfileResponse)
                .toList();
    }

    public UserProfileResponse updateUser(Long targetUserId, AdminUpdateUserRequest request) {
        UserEntity user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        if (request.active() != null) {
            user.setActive(request.active());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }

        UserEntity saved = userRepository.save(user);
        return toProfileResponse(saved);
    }

    private UserProfileResponse toProfileResponse(UserEntity user) {
        return new UserProfileResponse(
                user.getId(),
                user.getLoginId(),
                user.getEmail(),
                user.isEmailVerified(),
                user.getPhone(),
                user.isPhoneVerified(),
                user.getName(),
                user.getNickname(),
                user.getBirth(),
                user.getAddress(),
                user.getProfileImageUrl(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
