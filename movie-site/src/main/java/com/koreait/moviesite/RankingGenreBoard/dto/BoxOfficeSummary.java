package com.koreait.moviesite.RankingGenreBoard.dto;

import java.time.LocalDate;

/**
 * BoxOfficeAlltime 요약 조회용 인터페이스 프로젝션
 * findAllProjectedBy(Pageable)와 함께 사용됩니다.
 */
public interface BoxOfficeSummary {
    Integer getRankNo();
    String  getMovieNm();
    LocalDate getOpenDt();
    Long    getSalesAcc();
    Long    getAudiAcc();
    Integer getScreenCnt();
}
