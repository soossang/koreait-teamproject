package com.koreait.moviesite.DetailpageReserve.config;

import com.koreait.moviesite.Member.dao.MemberRepository;
import com.koreait.moviesite.Member.entity.MemberEntity;
import com.koreait.moviesite.Member.entity.MemberRole;
import com.koreait.moviesite.Member.security.AuthenticatedMember;
import com.koreait.moviesite.Member.security.JwtAuthenticationFilter;
import com.koreait.moviesite.Member.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

@WebMvcTest(controllers = SecurityTestController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class SecurityConfigTest {

    @Autowired MockMvc mockMvc;
    @MockBean JwtTokenProvider jwtTokenProvider;
    @MockBean MemberRepository memberRepository;

    @Test
    void memberApiRequiresValidJwt() throws Exception {
        mockMvc.perform(get("/api/member/test"))
                .andExpect(status().isUnauthorized());

        allowToken("user-token", MemberRole.USER);
        mockMvc.perform(get("/api/member/test").header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk());
    }

    @Test
    void adminApiRejectsUserAndAllowsAdmin() throws Exception {
        allowToken("user-token", MemberRole.USER);
        mockMvc.perform(get("/api/admin/test").header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());

        allowToken("admin-token", MemberRole.ADMIN);
        mockMvc.perform(get("/api/admin/test").header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk());
    }

    @Test
    void browserFormPostRequiresCsrfToken() throws Exception {
        mockMvc.perform(post("/board/test"))
                .andExpect(status().isForbidden());

        var csrfResult = mockMvc.perform(get("/csrf-token"))
                .andExpect(status().isOk())
                .andReturn();
        String parameterName = csrfResult.getResponse().getHeader("X-Test-Csrf-Parameter");
        String token = csrfResult.getResponse().getHeader("X-Test-Csrf-Token");
        MockHttpSession session = (MockHttpSession) csrfResult.getRequest().getSession(false);

        mockMvc.perform(post("/board/test")
                        .session(session)
                        .param(parameterName, token))
                .andExpect(status().isOk());
    }

    private void allowToken(String token, MemberRole role) {
        MemberEntity member = new MemberEntity();
        member.setLoginId("tester");
        member.setRole(role);
        member.setActive(true);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getAuthenticatedMember(token))
                .thenReturn(new AuthenticatedMember(1L, "tester", role));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
    }
}

@RestController
class SecurityTestController {
    @GetMapping("/api/member/test")
    String member() { return "member"; }

    @GetMapping("/api/admin/test")
    String admin() { return "admin"; }

    @PostMapping("/board/test")
    String boardPost() { return "ok"; }

    @GetMapping("/csrf-token")
    void csrfToken(CsrfToken csrfToken, HttpServletResponse response) {
        response.setHeader("X-Test-Csrf-Parameter", csrfToken.getParameterName());
        response.setHeader("X-Test-Csrf-Token", csrfToken.getToken());
    }
}
