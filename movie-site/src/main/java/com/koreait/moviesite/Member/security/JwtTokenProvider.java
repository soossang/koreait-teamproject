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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final List<SecretKey> verificationKeys;
    private final long validityInSeconds;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.previous-secret:}") String previousSecret,
            @Value("${jwt.expiration-seconds:3600}") long validityInSeconds
    ) {
        this.signingKey = toKey(secret, "JWT_SECRET");
        this.verificationKeys = new ArrayList<>();
        this.verificationKeys.add(signingKey);

        if (previousSecret != null && !previousSecret.isBlank() && !previousSecret.equals(secret)) {
            this.verificationKeys.add(toKey(previousSecret, "JWT_PREVIOUS_SECRET"));
        }
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
                .signWith(signingKey)
                .compact();
    }

    public boolean validateToken(String token) {
        for (SecretKey key : verificationKeys) {
            try {
                parseClaims(token, key);
                return true;
            } catch (Exception ignored) {
                // 키 교체 기간에는 현재 키와 이전 키를 순서대로 확인한다.
            }
        }
        return false;
    }

    public AuthenticatedMember getAuthenticatedMember(String token) {
        Claims claims = parseWithAnyVerificationKey(token);

        Long memberId = Long.valueOf(claims.getSubject());
        String loginId = claims.get("loginId", String.class);
        String roleStr = claims.get("role", String.class);
        MemberRole role = MemberRole.valueOf(roleStr);

        return new AuthenticatedMember(memberId, loginId, role);
    }

    public long getValidityInSeconds() {
        return validityInSeconds;
    }

    private Claims parseWithAnyVerificationKey(String token) {
        RuntimeException lastException = null;

        for (SecretKey key : verificationKeys) {
            try {
                return parseClaims(token, key);
            } catch (RuntimeException e) {
                lastException = e;
            }
        }

        throw lastException != null ? lastException : new IllegalArgumentException("유효하지 않은 JWT입니다.");
    }

    private Claims parseClaims(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey toKey(String secret, String settingName) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(settingName + " 값이 필요합니다.");
        }

        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(settingName + " 값은 32바이트 이상이어야 합니다.");
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}
