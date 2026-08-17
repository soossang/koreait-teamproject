package com.koreait.moviesite.Member.service;

import com.koreait.moviesite.Member.dao.MemberRepository;
import com.koreait.moviesite.Member.dto.AccountUpdateRequest;
import com.koreait.moviesite.Member.entity.MemberEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock PasswordEncoder passwordEncoder;

    MemberService memberService;
    MemberEntity member;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository, passwordEncoder);
        member = new MemberEntity();
        member.setLoginId("test1");
        member.setPassword("encoded-old-password");
        member.setEmail("old@example.com");
        member.setPhone("01011112222");
        member.setEmailVerified(true);
        member.setPhoneVerified(true);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
    }

    @Test
    void accountUpdateRequiresCurrentPassword() {
        when(passwordEncoder.matches("wrong", "encoded-old-password")).thenReturn(false);

        var request = new AccountUpdateRequest(
                "wrong", null, null, "new@example.com", "01033334444"
        );

        assertThatThrownBy(() -> memberService.updateAccount(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("현재 비밀번호");
        verify(memberRepository, never()).save(any());
    }

    @Test
    void contactAndPasswordAreUpdatedTogether() {
        when(passwordEncoder.matches("current", "encoded-old-password")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");
        when(memberRepository.save(any(MemberEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var request = new AccountUpdateRequest(
                "current", "new-password", "new-password", "NEW@EXAMPLE.COM", "010-3333-4444"
        );

        var response = memberService.updateAccount(1L, request);

        assertThat(response.email()).isEqualTo("new@example.com");
        assertThat(response.phone()).isEqualTo("01033334444");
        assertThat(member.getPassword()).isEqualTo("encoded-new-password");
        assertThat(member.isEmailVerified()).isFalse();
        assertThat(member.isPhoneVerified()).isFalse();
    }
}
