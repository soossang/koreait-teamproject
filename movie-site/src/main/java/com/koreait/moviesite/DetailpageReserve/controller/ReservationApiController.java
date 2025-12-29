package com.koreait.moviesite.DetailpageReserve.controller;

import com.koreait.moviesite.DetailpageReserve.domain.Reservation;
import com.koreait.moviesite.DetailpageReserve.dto.ReservationCreateRequest;
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
import java.util.Objects;

@RestController
@RequestMapping("/api/reservations")
public class ReservationApiController {

    private final ReservationService reservationService;
    private final MemberService memberService;

    public ReservationApiController(ReservationService reservationService, MemberService memberService) {
        this.reservationService = reservationService;
        this.memberService = memberService;
    }

    /**
     * (A+B) 로그인한 상태에서 예매 생성
     * - Authorization(Bearer 토큰) 필수 (/api/reservations/** 는 인터셉터 보호)
     * - 예매는 member_id로 계정에 연결
     */
    @PostMapping
    public ResponseEntity<?> createReservation(
            @RequestAttribute("authMember") AuthenticatedMember authMember,
            @RequestBody ReservationCreateRequest request
    ) {
        try {
            Reservation reservation = reservationService.reserveForMember(
                    authMember.id(),
                    request.screeningId(),
                    request.name(),
                    request.phone(),
                    request.count(),
                    request.seats()
            );
            return ResponseEntity.ok(Map.of(
                    "reservationNumber", reservation.getReservationNumber()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{reservationNumber}")
    public ResponseEntity<?> getReservation(
            @RequestAttribute("authMember") AuthenticatedMember authMember,
            @PathVariable("reservationNumber") String reservationNumber
    ) {
        try {
            Reservation r = reservationService.getByReservationNumber(reservationNumber);
            if (!isOwner(authMember, r)) {
                return ResponseEntity.status(403).body(Map.of("message", "권한이 없습니다."));
            }
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
            Reservation existing = reservationService.getByReservationNumber(reservationNumber);
            if (!isOwner(authMember, existing)) {
                return ResponseEntity.status(403).body(Map.of("message", "권한이 없습니다."));
            }
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

        // 1) member_id로 먼저 조회 (정확)
        var byMember = reservationService.findByMemberId(authMember.id());

        // 2) 기존 데이터/비회원 예매 호환: member_id로 결과가 없으면 phone으로 fallback
        var source = (!byMember.isEmpty())
                ? byMember
                : reservationService.findByPhoneVariants(phone);

        if (source.isEmpty()) {
            // 결과가 없을 때도 200 [] 로 내려주면 프론트에서 "예매 내역 없음" 처리하기 쉬움
            return ResponseEntity.ok(java.util.List.of());
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return ResponseEntity.ok(
                source.stream()
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
            Reservation existing = reservationService.getByReservationNumber(reservationNumber);
            if (!isOwner(authMember, existing)) {
                return ResponseEntity.status(403).body(Map.of("message", "권한이 없습니다."));
            }
            reservationService.cancelByReservationNumber(reservationNumber);
            return ResponseEntity.ok(Map.of("message", "예매가 취소되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * (A+B) 예매 접근 권한
     * 1) member_id가 있으면: 로그인한 계정과 일치해야 함
     * 2) member_id가 없으면(기존/비회원 데이터): 로그인한 계정 phone과 예매 phone이 같을 때만 허용
     */
    private boolean isOwner(AuthenticatedMember authMember, Reservation reservation) {
        if (reservation.getMember() != null) {
            return Objects.equals(reservation.getMember().getId(), authMember.id());
        }

        MemberProfileResponse me = memberService.getProfile(authMember.id());
        if (me == null || me.phone() == null) return false;

        String myPhone = me.phone().replaceAll("\\D", "");
        String reservationPhone = reservation.getPhone() == null ? "" : reservation.getPhone().replaceAll("\\D", "");
        return !myPhone.isBlank() && myPhone.equals(reservationPhone);
    }

}
