package com.koreait.moviesite.Member.security;

import com.koreait.moviesite.Member.dao.MemberRepository;
import com.koreait.moviesite.Member.entity.MemberEntity;
import com.koreait.moviesite.Member.entity.MemberRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock MemberRepository memberRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validBearerTokenCreatesSpringSecurityAuthentication() throws Exception {
        var member = new AuthenticatedMember(1L, "tester", MemberRole.ADMIN);
        var entity = new MemberEntity();
        entity.setLoginId("tester");
        entity.setRole(MemberRole.ADMIN);
        entity.setActive(true);
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.getAuthenticatedMember("valid-token")).thenReturn(member);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(entity));

        var request = new MockHttpServletRequest("GET", "/api/admin/test");
        request.addHeader("Authorization", "Bearer valid-token");

        new JwtAuthenticationFilter(jwtTokenProvider, memberRepository).doFilter(
                request, new MockHttpServletResponse(), new MockFilterChain()
        );

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal())
                .usingRecursiveComparison()
                .isEqualTo(member);
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
        assertThat(request.getAttribute("authMember")).isEqualTo(member);
    }

    @Test
    void invalidTokenDoesNotAuthenticateRequest() throws Exception {
        when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(false);
        var request = new MockHttpServletRequest("GET", "/api/member/me");
        request.addHeader("Authorization", "Bearer invalid-token");

        new JwtAuthenticationFilter(jwtTokenProvider, memberRepository).doFilter(
                request, new MockHttpServletResponse(), new MockFilterChain()
        );

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtTokenProvider).validateToken("invalid-token");
    }

    @Test
    void tokenForDeletedMemberDoesNotAuthenticateRequest() throws Exception {
        var tokenMember = new AuthenticatedMember(99L, "deleted", MemberRole.ADMIN);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(tokenMember, null, List.of())
        );
        when(jwtTokenProvider.validateToken("orphan-token")).thenReturn(true);
        when(jwtTokenProvider.getAuthenticatedMember("orphan-token")).thenReturn(tokenMember);
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new MockHttpServletRequest("GET", "/api/member/me");
        request.addHeader("Authorization", "Bearer orphan-token");

        new JwtAuthenticationFilter(jwtTokenProvider, memberRepository).doFilter(
                request, new MockHttpServletResponse(), new MockFilterChain()
        );

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(request.getAttribute("authMember")).isNull();
    }

    @Test
    void validBearerTokenRefreshesRequestAttributeWhenSessionIsAlreadyAuthenticated() throws Exception {
        var stalePrincipal = new AuthenticatedMember(1L, "tester", MemberRole.USER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(stalePrincipal, null, List.of())
        );

        var entity = new MemberEntity();
        entity.setLoginId("tester");
        entity.setRole(MemberRole.ADMIN);
        entity.setActive(true);
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.getAuthenticatedMember("valid-token"))
                .thenReturn(new AuthenticatedMember(1L, "tester", MemberRole.ADMIN));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(entity));

        var request = new MockHttpServletRequest("GET", "/api/admin/member");
        request.addHeader("Authorization", "Bearer valid-token");

        new JwtAuthenticationFilter(jwtTokenProvider, memberRepository).doFilter(
                request, new MockHttpServletResponse(), new MockFilterChain()
        );

        var refreshed = (AuthenticatedMember) request.getAttribute("authMember");
        assertThat(refreshed.role()).isEqualTo(MemberRole.ADMIN);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo(refreshed);
    }
}
