package com.koreait.moviesite.Member.service;

import com.koreait.moviesite.Member.dao.MemberRepository;
import com.koreait.moviesite.Member.entity.MemberEntity;
import com.koreait.moviesite.Member.entity.MemberRole;
import com.koreait.moviesite.Member.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtTokenProvider jwtTokenProvider;

    @Test
    void activeLoginSessionCanReceiveReplacementToken() {
        MemberEntity member = new MemberEntity();
        member.setLoginId("test1");
        member.setRole(MemberRole.USER);
        member.setActive(true);

        when(memberRepository.findByLoginId("test1")).thenReturn(Optional.of(member));
        when(jwtTokenProvider.generateToken(member)).thenReturn("replacement-token");
        when(jwtTokenProvider.getValidityInSeconds()).thenReturn(3600L);

        AuthService service = new AuthService(
                memberRepository, passwordEncoder, jwtTokenProvider, ""
        );

        var response = service.issueTokenForSession("test1");

        assertThat(response.getToken()).isEqualTo("replacement-token");
        assertThat(response.getLoginId()).isEqualTo("test1");
        assertThat(response.getRole()).isEqualTo("USER");
    }
}
