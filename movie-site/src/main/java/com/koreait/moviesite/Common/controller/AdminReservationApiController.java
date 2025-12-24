package com.koreait.moviesite.Common.controller;

import com.koreait.moviesite.DetailpageReserve.domain.Reservation;
import com.koreait.moviesite.DetailpageReserve.service.ReservationService;
import com.koreait.moviesite.Member.entity.MemberRole;
import com.koreait.moviesite.Member.security.AuthenticatedMember;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reservations")
public class AdminReservationApiController {

    private final ReservationService reservationService;

    public AdminReservationApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    private boolean isAdmin(AuthenticatedMember auth) {
        return auth != null && auth.role() == MemberRole.ADMIN;
    }

    public record ReservationAdminRow(
            String reservationNumber,
            Long screeningId,
            String movieTitle,
            String theaterName,
            LocalDateTime screeningTime,
            String name,
            String phone,
            int reservedCount,
            String seats,
            LocalDateTime reservedAt
    ) {}

    @GetMapping
    public ResponseEntity<?> listAll(
            @RequestAttribute("authMember") AuthenticatedMember authMember
    ) {
        if (!isAdmin(authMember)) {
            return ResponseEntity.status(403).build();
        }
        List<Reservation> list = reservationService.findAll(Sort.by(Sort.Direction.DESC, "reservedAt"));
        return ResponseEntity.ok(
                list.stream().map(r -> new ReservationAdminRow(
                        r.getReservationNumber(),
                        r.getScreening() != null ? r.getScreening().getId() : null,
                        (r.getScreening() != null && r.getScreening().getMovie() != null) ? r.getScreening().getMovie().getTitle() : null,
                        r.getScreening() != null ? r.getScreening().getTheaterName() : null,
                        r.getScreening() != null ? r.getScreening().getScreeningTime() : null,
                        r.getName(),
                        r.getPhone(),
                        r.getReservedCount(),
                        r.getSeats(),
                        r.getReservedAt()
                )).toList()
        );
    }

    @DeleteMapping("/{reservationNumber}")
    public ResponseEntity<?> cancel(
            @RequestAttribute("authMember") AuthenticatedMember authMember,
            @PathVariable("reservationNumber") String reservationNumber
    ) {
        if (!isAdmin(authMember)) {
            return ResponseEntity.status(403).build();
        }
        try {
            reservationService.cancelByReservationNumber(reservationNumber);
            return ResponseEntity.ok(Map.of("message", "예매가 취소되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
