package com.koreait.moviesite.DetailpageReserve.repository;

import com.koreait.moviesite.DetailpageReserve.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 특정 상영 회차의 모든 예약 조회
    List<Reservation> findByScreeningId(Long screeningId);

    // ★ 특정 상영 회차의 예약 인원 수 합계
    @Query("select coalesce(sum(r.reservedCount), 0) from Reservation r where r.screening.id = :screeningId")
    int sumReservedCountByScreeningId(@Param("screeningId") Long screeningId);
}
