package com.koreait.moviesite.Member.controller;

import com.koreait.moviesite.Member.dto.MemberProfileResponse;
import com.koreait.moviesite.Member.dto.MemberUpdateRequest;
import com.koreait.moviesite.Member.dto.PasswordChangeRequest;
import com.koreait.moviesite.Member.security.AuthenticatedMember;
import com.koreait.moviesite.Member.service.MemberProfileImageService;
import com.koreait.moviesite.Member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;
    private final MemberProfileImageService memberProfileImageService;

    public MemberController(MemberService memberService,
                            MemberProfileImageService memberProfileImageService) {
        this.memberService = memberService;
        this.memberProfileImageService = memberProfileImageService;
    }

    @GetMapping("/me")
    public ResponseEntity<MemberProfileResponse> getMe(
            @RequestAttribute("authMember") AuthenticatedMember authMember
    ) {
        MemberProfileResponse profile = memberService.getProfile(authMember.id());
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/me")
    public ResponseEntity<MemberProfileResponse> updateMe(
            @RequestAttribute("authMember") AuthenticatedMember authMember,
            @RequestBody MemberUpdateRequest request
    ) {
        MemberProfileResponse profile = memberService.updateProfile(authMember.id(), request);
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @RequestAttribute("authMember") AuthenticatedMember authMember,
            @RequestBody PasswordChangeRequest request
    ) {
        memberService.changePassword(authMember.id(), request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/profile-image")
    public ResponseEntity<String> uploadProfileImage(
            @RequestAttribute("authMember") AuthenticatedMember authMember,
            @RequestPart("file") MultipartFile file
    ) throws Exception {
        String url = memberProfileImageService.uploadProfileImage(authMember.id(), file);
        return ResponseEntity.ok(url);
    }
}
