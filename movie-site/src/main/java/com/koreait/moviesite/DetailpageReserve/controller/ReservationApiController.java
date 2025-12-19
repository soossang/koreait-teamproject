package com.koreait.moviesite.DetailpageReserve.controller;

import com.koreait.moviesite.DetailpageReserve.domain.Reservation;
import com.koreait.moviesite.DetailpageReserve.dto.ReservationStatusResponse;
import com.koreait.moviesite.DetailpageReserve.dto.ReservationNumberLookupResponse;
import com.koreait.moviesite.DetailpageReserve.dto.ReservationUpdateRequest;
import com.koreait.moviesite.DetailpageReserve.service.ReservationService;
import com.koreait.moviesite.Member.security.AuthenticatedMember;
import com.koreait.moviesite.Member.service.MemberService;
import com.koreait.moviesite.Member.dto.MemberProfileResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationApiController {

    private final ReservationService reservationService;
    private final MemberService memberService;

    public ReservationApiController(ReservationService reservationService, MemberService memberService) {
        this.reservationService = reservationService;
        this.memberService = memberService;
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


    // ===== 마이페이지: 내 휴대폰 번호로 예매번호 찾기 =====
    @GetMapping("/my-numbers")
    public ResponseEntity<?> getMyReservationNumbers(
            @RequestAttribute("authMember") AuthenticatedMember authMember
    ) {
        MemberProfileResponse me = memberService.getProfile(authMember.id());
        String phone = me.phone();
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "회원 휴대폰 정보가 없습니다. 회원정보에서 휴대폰을 등록해 주세요."));
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return ResponseEntity.ok(
                reservationService.findByPhoneVariants(phone).stream()
                        .map(r -> new ReservationNumberLookupResponse(
                                r.getReservationNumber(),
                                r.getScreening().getMovie().getTitle(),
                                r.getScreening().getTheaterName(),
                                r.getScreening().getScreeningTime().format(fmt),
                                r.getReservedAt() != null ? r.getReservedAt().format(fmt) : null,
                                r.getReservedCount(),
                                r.getSeats(),
                                r.getName(),
                                r.getPhone()
                        ))
                        .toList()
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
