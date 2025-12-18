package com.koreait.moviesite.DetailpageReserve.controller;

import com.koreait.moviesite.DetailpageReserve.domain.Reservation;
import com.koreait.moviesite.DetailpageReserve.repository.ReservationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/screenings")
public class ScreeningApiController {

    private final ReservationRepository reservationRepository;

    public ScreeningApiController(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @GetMapping("/{screeningId}/reserved-seats")
    public ResponseEntity<?> reservedSeats(
            @PathVariable("screeningId") Long screeningId,
            @RequestParam(value = "excludeReservationNumber", required = false) String excludeReservationNumber
    ) {
        List<Reservation> reservations = reservationRepository.findByScreeningId(screeningId);

        Set<String> reservedSeats = reservations.stream()
                .filter(r -> r.getSeats() != null)
                // ✅ 내 예매(현재 조회한 예매)의 좌석은 제외하고 막기
                .filter(r -> excludeReservationNumber == null
                        || !Objects.equals(excludeReservationNumber, r.getReservationNumber()))
                .flatMap(r -> Arrays.stream(r.getSeats().split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return ResponseEntity.ok(Map.of(
                "screeningId", screeningId,
                "reservedSeats", reservedSeats
        ));
    }
}
