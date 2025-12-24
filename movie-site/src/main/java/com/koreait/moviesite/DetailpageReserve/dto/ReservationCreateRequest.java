package com.koreait.moviesite.DetailpageReserve.dto;

/**
 * (A+B) 예매 생성 요청 DTO
 * - 프론트에서 좌석선택 페이지(form.html)에서 fetch로 전송
 * - phone은 서버에서 회원 phone으로 덮어쓸 수 있음(일관성 유지)
 */
public record ReservationCreateRequest(
        Long screeningId,
        String name,
        String phone,
        int count,
        String seats
) {
}
