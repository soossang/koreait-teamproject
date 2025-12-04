package com.koreait.moviesite.Member.controller;

import com.koreait.moviesite.Member.dto.AdminUpdateMemberRequest;
import com.koreait.moviesite.Member.dto.MemberProfileResponse;
import com.koreait.moviesite.Member.entity.MemberRole;
import com.koreait.moviesite.Member.security.AuthenticatedMember;
import com.koreait.moviesite.Member.service.AdminMemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/member")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    public AdminMemberController(AdminMemberService adminMemberService) {
        this.adminMemberService = adminMemberService;
    }

    @GetMapping
    public ResponseEntity<List<MemberProfileResponse>> getAllMembers(
            @RequestAttribute("authMember") AuthenticatedMember authMember
    ) {
        if (authMember.role() != MemberRole.ADMIN) {
            return ResponseEntity.status(403).build();
        }
        List<MemberProfileResponse> members = adminMemberService.getAllMembers();
        return ResponseEntity.ok(members);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<MemberProfileResponse> updateMember(
            @RequestAttribute("authMember") AuthenticatedMember authMember,
            @PathVariable Long id,
            @RequestBody AdminUpdateMemberRequest request
    ) {
        if (authMember.role() != MemberRole.ADMIN) {
            return ResponseEntity.status(403).build();
        }
        MemberProfileResponse updated = adminMemberService.updateMember(id, request);
        return ResponseEntity.ok(updated);
    }
}
