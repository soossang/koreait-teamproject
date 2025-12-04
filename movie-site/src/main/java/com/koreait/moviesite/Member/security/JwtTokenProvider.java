package com.koreait.moviesite.Member.security;

import com.koreait.moviesite.Member.entity.MemberEntity;
import com.koreait.moviesite.Member.entity.MemberRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final byte[] secretKeyBytes;
    private final long validityInSeconds;

    public JwtTokenProvider(
            @Value("${jwt.secret:change-this-secret-key}") String secretKey,
            @Value("${jwt.expiration-seconds:3600}") long validityInSeconds
    ) {
        this.secretKeyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.validityInSeconds = validityInSeconds;
    }

    public String generateToken(MemberEntity member) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(validityInSeconds);

        return Jwts.builder()
                .subject(String.valueOf(member.getId()))
                .claim("loginId", member.getLoginId())
                .claim("role", member.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .issuer("movie-site")
                .signWith(Keys.hmacShaKeyFor(secretKeyBytes))
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secretKeyBytes))
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public AuthenticatedMember getAuthenticatedMember(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKeyBytes))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long memberId = Long.valueOf(claims.getSubject());
        String loginId = claims.get("loginId", String.class);
        String roleStr = claims.get("role", String.class);
        MemberRole role = MemberRole.valueOf(roleStr);

        return new AuthenticatedMember(memberId, loginId, role);
    }

    public long getValidityInSeconds() {
        return validityInSeconds;
    }
}
