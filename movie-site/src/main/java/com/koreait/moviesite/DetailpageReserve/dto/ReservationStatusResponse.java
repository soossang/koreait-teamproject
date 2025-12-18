package com.koreait.moviesite.DetailpageReserve.dto;

/**
 * 마이페이지 예매 조회/수정 화면에서 사용하는 응답 DTO
 */
public record ReservationStatusResponse(
        String reservationNumber,
        Long screeningId,
        String movieTitle,
        String theaterName,
        String screeningTime,
        String name,
        String phone,
        int reservedCount,
        String seats
) {
}
