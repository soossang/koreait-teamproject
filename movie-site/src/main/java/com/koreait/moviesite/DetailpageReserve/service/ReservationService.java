package com.koreait.moviesite.DetailpageReserve.service;

import com.koreait.movie_project.domain.Reservation;
import com.koreait.movie_project.domain.Screening;
import com.koreait.movie_project.repository.ReservationRepository;
import com.koreait.movie_project.repository.ScreeningRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ScreeningRepository screeningRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ScreeningRepository screeningRepository) {
        this.reservationRepository = reservationRepository;
        this.screeningRepository = screeningRepository;
    }
    
    @Transactional(readOnly = true)
    public int getAvailableSeats(Long screeningId) {
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new IllegalArgumentException("상영 정보를 찾을 수 없습니다. id=" + screeningId));

        int total = screening.getTotalSeats();
        int reserved = reservationRepository.sumReservedCountByScreeningId(screeningId);

        return total - reserved;
    }
    

    // 좌석 포함 예매
    @Transactional
    public Reservation reserve(Long screeningId, String name,
                               String phone, int count, String seats) {

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new IllegalArgumentException("상영 정보를 찾을 수 없습니다. id=" + screeningId));

        // 사용자가 선택한 좌석 목록
        List<String> selectedSeats = Arrays.stream(seats.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (selectedSeats.size() != count) {
            throw new IllegalStateException("선택한 좌석 수와 인원 수가 다릅니다.");
        }

        // 이미 예약된 좌석 목록
        List<Reservation> reservations = reservationRepository.findByScreeningId(screeningId);
        Set<String> reservedSeats = reservations.stream()
                .filter(r -> r.getSeats() != null)
                .flatMap(r -> Arrays.stream(r.getSeats().split(",")))
                .map(String::trim)
                .collect(Collectors.toSet());

        // 중복 좌석 체크
        for (String seat : selectedSeats) {
            if (reservedSeats.contains(seat)) {
                throw new IllegalStateException("이미 예약된 좌석이 포함되어 있습니다: " + seat);
            }
        }

        // 잔여 좌석 확인 (예약 테이블 기준으로 계산)
        int availableSeats = getAvailableSeats(screeningId);
        if (availableSeats < count) {
            throw new IllegalStateException("잔여 좌석이 부족합니다.");
        }

        Reservation reservation =
                new Reservation(screening, name, phone, count, seats);
        return reservationRepository.save(reservation);
    }
    
    
    
}
