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
        return ResponseEntity.ok(adminMemberService.getAllMembers());
    }

    // 활성 상태 변경
    @PatchMapping("/{id}")
    public ResponseEntity<MemberProfileResponse> updateMember(
            @RequestAttribute("authMember") AuthenticatedMember authMember,
            @PathVariable("id") Long id,
            @RequestBody AdminUpdateMemberRequest request
    ) {
        if (authMember.role() != MemberRole.ADMIN) {
            return ResponseEntity.status(403).build();
        }
        MemberProfileResponse updated = adminMemberService.updateMember(id, request);
        return ResponseEntity.ok(updated);
    }

    // 회원 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(
            @RequestAttribute("authMember") AuthenticatedMember authMember,
            @PathVariable("id") Long id
    ) {
        if (authMember.role() != MemberRole.ADMIN) {
            return ResponseEntity.status(403).build();
        }
        adminMemberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}
