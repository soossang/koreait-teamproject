package com.koreait.moviesite.DetailpageReserve.repository;

import com.koreait.moviesite.DetailpageReserve.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 특정 상영 회차의 모든 예약 조회
    List<Reservation> findByScreeningId(Long screeningId);

    // ★ 특정 상영 회차의 예약 인원 수 합계
    @Query("select coalesce(sum(r.reservedCount), 0) from Reservation r where r.screening.id = :screeningId")
    int sumReservedCountByScreeningId(@Param("screeningId") Long screeningId);

    // 예매 조회용 (10자리 랜덤 코드)
    Optional<Reservation> findByReservationNumber(String reservationNumber);
    boolean existsByReservationNumber(String reservationNumber);


    // (B) 마이페이지 예매번호 찾기: 계정(member_id)으로 먼저 조회
    @Query("select r from Reservation r where r.member.id = :memberId order by r.reservedAt desc")
    List<Reservation> findByMemberIdOrderByReservedAtDesc(@Param("memberId") Long memberId);

    // (A 보완) 휴대폰 번호로 조회 (기존 데이터/비회원 예매 fallback)
    // - trim(r.phone) 적용: DB에 공백이 섞여 저장된 케이스도 잡아주기
    @Query("select r from Reservation r where trim(r.phone) in :phones order by r.reservedAt desc")
    List<Reservation> findByPhoneInOrderByReservedAtDesc(@Param("phones") List<String> phones);
}
