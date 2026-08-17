package com.koreait.moviesite.Member.security;

import com.koreait.moviesite.Member.dao.MemberRepository;
import com.koreait.moviesite.Member.entity.MemberEntity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   MemberRepository memberRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberRepository = memberRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null) {
            if (jwtTokenProvider.validateToken(token)) {
                AuthenticatedMember tokenMember = jwtTokenProvider.getAuthenticatedMember(token);
                Optional<MemberEntity> currentMember = memberRepository.findById(tokenMember.id());

                if (currentMember.isPresent() && currentMember.get().isActive()) {
                    MemberEntity entity = currentMember.get();
                    AuthenticatedMember member = new AuthenticatedMember(
                            tokenMember.id(), entity.getLoginId(), entity.getRole()
                    );
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + member.role().name()));
                    var authentication = new UsernamePasswordAuthenticationToken(member, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    request.setAttribute("authMember", member);
                } else {
                    SecurityContextHolder.clearContext();
                }
            } else {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }

        String token = header.substring(7).trim();
        return token.isEmpty() ? null : token;
    }
}
