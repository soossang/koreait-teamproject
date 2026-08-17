package com.koreait.moviesite.DetailpageReserve.service;

import com.koreait.moviesite.DetailpageReserve.domain.Reservation;
import com.koreait.moviesite.DetailpageReserve.domain.Screening;
import com.koreait.moviesite.DetailpageReserve.repository.ReservationRepository;
import com.koreait.moviesite.DetailpageReserve.repository.ScreeningRepository;
import com.koreait.moviesite.Member.dao.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock ReservationRepository reservationRepository;
    @Mock ScreeningRepository screeningRepository;
    @Mock MemberRepository memberRepository;
    @Mock Screening screening;

    ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                reservationRepository, screeningRepository, memberRepository
        );
        when(screeningRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(screening));
    }

    @Test
    void reservationLocksScreeningAndRejectsAlreadyReservedSeat() {
        when(screening.getId()).thenReturn(1L);
        Reservation existing = mock(Reservation.class);
        when(existing.getSeats()).thenReturn("A1,A2");
        when(reservationRepository.findByScreeningId(1L)).thenReturn(List.of(existing));

        assertThatThrownBy(() -> reservationService.reserve(1L, "홍길동", "01012345678", 1, "A1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 예약된 좌석");

        verify(screeningRepository).findByIdForUpdate(1L);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void duplicateSeatsInSingleRequestAreRejected() {
        assertThatThrownBy(() -> reservationService.reserve(1L, "홍길동", "01012345678", 2, "A1,A1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("중복 선택");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void validSeatsAreNormalizedBeforeSave() {
        when(screening.getId()).thenReturn(1L);
        when(screening.getTotalSeats()).thenReturn(50);
        when(reservationRepository.findByScreeningId(1L)).thenReturn(List.of());
        when(reservationRepository.existsByReservationNumber(anyString())).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation saved = reservationService.reserve(1L, "홍길동", "010-1234-5678", 2, "a1, a2");

        assertThat(saved.getSeats()).isEqualTo("A1,A2");
        assertThat(saved.getPhone()).isEqualTo("01012345678");
    }
}
