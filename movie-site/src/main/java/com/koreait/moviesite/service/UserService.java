package com.koreait.moviesite.service;

import com.koreait.moviesite.dao.UserRepository;
import com.koreait.moviesite.dto.PasswordChangeRequest;
import com.koreait.moviesite.dto.UserProfileResponse;
import com.koreait.moviesite.dto.UserUpdateRequest;
import com.koreait.moviesite.entity.UserEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserProfileResponse getProfile(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        return toProfileResponse(user);
    }

    public UserProfileResponse updateProfile(Long userId, UserUpdateRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        if (request.name() != null) user.setName(request.name());
        if (request.nickname() != null) user.setNickname(request.nickname());
        if (request.birth() != null) user.setBirth(request.birth());
        if (request.address() != null) user.setAddress(request.address());
        if (request.profileImageUrl() != null) user.setProfileImageUrl(request.profileImageUrl());

        UserEntity saved = userRepository.save(user);
        return toProfileResponse(saved);
    }

    public void changePassword(Long userId, PasswordChangeRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
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
