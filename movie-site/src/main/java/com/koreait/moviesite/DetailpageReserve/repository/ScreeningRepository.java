package com.koreait.moviesite.DetailpageReserve.repository;

import com.koreait.moviesite.DetailpageReserve.domain.Screening;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {
    // 특정 영화의 상영정보 목록
    List<Screening> findByMovieId(Long movieId);
    
 // 상영시간 오름차순 정렬해서 가져오기
    List<Screening> findByMovieIdOrderByScreeningTimeAsc(Long movieId);
}
