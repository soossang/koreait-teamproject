package com.koreait.moviesite.DetailpageReserve.service;

import com.koreait.moviesite.DetailpageReserve.domain.Reservation;
import com.koreait.moviesite.DetailpageReserve.domain.Screening;
import com.koreait.moviesite.DetailpageReserve.repository.ReservationRepository;
import com.koreait.moviesite.DetailpageReserve.repository.ScreeningRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ScreeningRepository screeningRepository;

    private static final String RESERVATION_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final SecureRandom secureRandom = new SecureRandom();

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

        String reservationNumber = generateUniqueReservationNumber(10);

        Reservation reservation =
                new Reservation(screening, reservationNumber, name, phone, count, seats);
        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public Reservation getByReservationNumber(String reservationNumber) {
        return reservationRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new IllegalArgumentException("예매 정보를 찾을 수 없습니다. 예매번호=" + reservationNumber));
    }

    @Transactional
    public Reservation updateByReservationNumber(String reservationNumber,
                                                String name,
                                                String phone,
                                                int reservedCount,
                                                String seats) {

        Reservation reservation = getByReservationNumber(reservationNumber);
        Long screeningId = reservation.getScreening().getId();

        // 사용자가 선택한 좌석 목록
        List<String> selectedSeats = Arrays.stream(seats.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (selectedSeats.size() != reservedCount) {
            throw new IllegalStateException("선택한 좌석 수와 인원 수가 다릅니다.");
        }

        // 이미 예약된 좌석 목록(본인 예약 제외)
        List<Reservation> reservations = reservationRepository.findByScreeningId(screeningId);
        Set<String> reservedSeats = reservations.stream()
                .filter(r -> !Objects.equals(r.getId(), reservation.getId()))
                .filter(r -> r.getSeats() != null)
                .flatMap(r -> Arrays.stream(r.getSeats().split(",")))
                .map(String::trim)
                .collect(Collectors.toSet());

        for (String seat : selectedSeats) {
            if (reservedSeats.contains(seat)) {
                throw new IllegalStateException("이미 예약된 좌석이 포함되어 있습니다: " + seat);
            }
        }

        reservation.updateInfo(name, phone, reservedCount, seats);
        return reservation;
    }

    
    @Transactional
    public void cancelByReservationNumber(String reservationNumber) {
        Reservation reservation = reservationRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new IllegalArgumentException("예매 정보를 찾을 수 없습니다. 예매번호=" + reservationNumber));
        reservationRepository.delete(reservation);
    }

private String generateUniqueReservationNumber(int length) {
        // 안전장치: 중복될 가능성은 낮지만, UNIQUE 컬럼이라 충돌 시 재시도
        for (int i = 0; i < 50; i++) {
            String candidate = randomReservationCode(length);
            if (!reservationRepository.existsByReservationNumber(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("예매번호 생성에 실패했습니다. 다시 시도해 주세요.");
    }

    private String randomReservationCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = secureRandom.nextInt(RESERVATION_CHARS.length());
            sb.append(RESERVATION_CHARS.charAt(idx));
        }
        return sb.toString();
    }
    
    
    
}
