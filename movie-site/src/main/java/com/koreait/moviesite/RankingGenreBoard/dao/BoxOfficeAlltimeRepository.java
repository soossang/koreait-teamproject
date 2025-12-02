package com.koreait.moviesite.RankingGenreBoard.dao;

import com.koreait.moviesite.RankingGenreBoard.dto.BoxOfficeSummary;
import com.koreait.moviesite.RankingGenreBoard.entity.BoxOfficeAlltime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface BoxOfficeAlltimeRepository extends JpaRepository<BoxOfficeAlltime, Integer> {

    /**
     * 메인 페이지용: 요약 페이지네이션 (인터페이스 프로젝션)
     * AlltimeService.page(...) 에서 사용
     */
    Page<BoxOfficeSummary> findAllProjectedBy(Pageable pageable);

    /**
     * 장르별: 영화 "제목만" 반환 (중복 제거)
     * GenreService.titles(...) 에서 사용
     */
    @Query("""
           select distinct b.movieNm
             from BoxOfficeAlltime b
             join b.genres g
            where upper(g.name) = upper(:genre)
           """)
    Page<String> findTitlesByGenre(@Param("genre") String genre, Pageable pageable);
}
