package com.koreait.moviesite.DetailpageReserve.dto;

/**
 * 마이페이지 '예매번호 찾기'에서 사용하는 응답 DTO
 */
public record ReservationNumberLookupResponse(
        String reservationNumber,
        String movieTitle,
        String theaterName,
        String screeningTime,
        String reservedAt,
        int reservedCount,
        String seats,
        String name,
        String phone
) {
}
