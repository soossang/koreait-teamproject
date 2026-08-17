package com.koreait.moviesite.Member.config;

import com.koreait.moviesite.Member.dao.MemberRepository;
import com.koreait.moviesite.Member.entity.MemberEntity;
import com.koreait.moviesite.Member.entity.MemberRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAccountInitializerTest {

    @Mock MemberRepository memberRepository;
    @Mock PasswordEncoder passwordEncoder;

    @Test
    void existingAdminIsNeverModified() {
        MemberEntity existing = new MemberEntity();
        existing.setLoginId("admin");
        existing.setPassword("already-encoded-password");
        existing.setRole(MemberRole.ADMIN);
        when(memberRepository.findByLoginId("admin")).thenReturn(Optional.of(existing));

        new AdminAccountInitializer(memberRepository, passwordEncoder, "admin", "new-password").run();

        assertThat(existing.getPassword()).isEqualTo("already-encoded-password");
        verify(passwordEncoder, never()).encode(anyString());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void missingAdminIsCreatedOnce() {
        when(memberRepository.findByLoginId("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("strong-password")).thenReturn("encoded-password");

        new AdminAccountInitializer(memberRepository, passwordEncoder, "admin", "strong-password").run();

        ArgumentCaptor<MemberEntity> captor = ArgumentCaptor.forClass(MemberEntity.class);
        verify(memberRepository).save(captor.capture());
        assertThat(captor.getValue().getLoginId()).isEqualTo("admin");
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded-password");
        assertThat(captor.getValue().getRole()).isEqualTo(MemberRole.ADMIN);
    }
}
