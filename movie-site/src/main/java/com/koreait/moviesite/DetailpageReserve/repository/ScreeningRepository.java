package com.koreait.moviesite.DetailpageReserve.repository;

import com.koreait.moviesite.DetailpageReserve.domain.Screening;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Screening s where s.id = :id")
    Optional<Screening> findByIdForUpdate(@Param("id") Long id);

    // 특정 영화의 상영정보 목록
    List<Screening> findByMovieId(Long movieId);
    
 // 상영시간 오름차순 정렬해서 가져오기
    List<Screening> findByMovieIdOrderByScreeningTimeAsc(Long movieId);
}
