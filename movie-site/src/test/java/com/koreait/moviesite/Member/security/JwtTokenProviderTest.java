package com.koreait.moviesite.Member.security;

import com.koreait.moviesite.Member.entity.MemberEntity;
import com.koreait.moviesite.Member.entity.MemberRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtTokenProviderTest {

    private static final String CURRENT_SECRET =
            "current-test-secret-key-with-at-least-32-bytes";
    private static final String PREVIOUS_SECRET =
            "previous-test-secret-key-with-at-least-32-bytes";

    @Test
    void acceptsTokenSignedWithPreviousKeyDuringRotation() {
        MemberEntity member = mock(MemberEntity.class);
        when(member.getId()).thenReturn(7L);
        when(member.getLoginId()).thenReturn("test1");
        when(member.getRole()).thenReturn(MemberRole.USER);

        JwtTokenProvider oldProvider = new JwtTokenProvider(PREVIOUS_SECRET, "", 3600);
        String oldToken = oldProvider.generateToken(member);

        JwtTokenProvider rotatedProvider =
                new JwtTokenProvider(CURRENT_SECRET, PREVIOUS_SECRET, 3600);

        assertThat(rotatedProvider.validateToken(oldToken)).isTrue();
        assertThat(rotatedProvider.getAuthenticatedMember(oldToken).loginId()).isEqualTo("test1");
    }

    @Test
    void newTokensUseCurrentKey() {
        MemberEntity member = mock(MemberEntity.class);
        when(member.getId()).thenReturn(8L);
        when(member.getLoginId()).thenReturn("admin");
        when(member.getRole()).thenReturn(MemberRole.ADMIN);

        JwtTokenProvider rotatedProvider =
                new JwtTokenProvider(CURRENT_SECRET, PREVIOUS_SECRET, 3600);
        String newToken = rotatedProvider.generateToken(member);

        assertThat(new JwtTokenProvider(CURRENT_SECRET, "", 3600).validateToken(newToken)).isTrue();
        assertThat(new JwtTokenProvider(PREVIOUS_SECRET, "", 3600).validateToken(newToken)).isFalse();
    }
}
