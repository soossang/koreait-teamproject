package com.koreait.moviesite.RankingGenreBoard.dto;

import java.time.LocalDate;

public interface BoxOfficeSummary {
    Integer getRankNo();
    String  getMovieNm();
    LocalDate getOpenDt();
    Long    getAudiAcc();
    Long    getSalesAcc();
    Integer getScreenCnt();
}
