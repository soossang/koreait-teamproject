package com.koreait.moviesite.DetailpageReserve.controller;

import com.koreait.moviesite.DetailpageReserve.domain.Reservation;
import com.koreait.moviesite.DetailpageReserve.dto.ReservationStatusResponse;
import com.koreait.moviesite.DetailpageReserve.dto.ReservationUpdateRequest;
import com.koreait.moviesite.DetailpageReserve.service.ReservationService;
import com.koreait.moviesite.Member.security.AuthenticatedMember;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationApiController {

    private final ReservationService reservationService;

    public ReservationApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{reservationNumber}")
    public ResponseEntity<?> getReservation(
            @RequestAttribute("authMember") AuthenticatedMember authMember,
            @PathVariable("reservationNumber") String reservationNumber
    ) {
        try {
            Reservation r = reservationService.getByReservationNumber(reservationNumber);
            return ResponseEntity.ok(toResponse(r));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{reservationNumber}")
    public ResponseEntity<?> updateReservation(
            @RequestAttribute("authMember") AuthenticatedMember authMember,
            @PathVariable("reservationNumber") String reservationNumber,
            @RequestBody ReservationUpdateRequest request
    ) {
        try {
            Reservation updated = reservationService.updateByReservationNumber(
                    reservationNumber,
                    request.name(),
                    request.phone(),
                    request.reservedCount(),
                    request.seats()
            );
            return ResponseEntity.ok(toResponse(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private ReservationStatusResponse toResponse(Reservation r) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return new ReservationStatusResponse(
                r.getReservationNumber(),
                r.getScreening().getId(),
                r.getScreening().getMovie().getTitle(),
                r.getScreening().getTheaterName(),
                r.getScreening().getScreeningTime().format(fmt),
                r.getName(),
                r.getPhone(),
                r.getReservedCount(),
                r.getSeats()
        );
    }


    @DeleteMapping("/{reservationNumber}")
    public ResponseEntity<?> cancelReservation(
            @RequestAttribute("authMember") AuthenticatedMember authMember,
            @PathVariable("reservationNumber") String reservationNumber
    ) {
        try {
            reservationService.cancelByReservationNumber(reservationNumber);
            return ResponseEntity.ok(Map.of("message", "예매가 취소되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

}
