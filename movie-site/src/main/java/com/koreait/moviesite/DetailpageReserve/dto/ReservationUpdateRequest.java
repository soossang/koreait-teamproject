package com.koreait.moviesite.DetailpageReserve.dto;

/**
 * 마이페이지에서 예매정보 수정할 때 사용하는 요청 DTO
 */
public record ReservationUpdateRequest(
        String name,
        String phone,
        int reservedCount,
        String seats
) {
}
