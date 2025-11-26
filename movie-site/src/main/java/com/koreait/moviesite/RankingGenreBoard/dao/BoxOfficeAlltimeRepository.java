package com.koreait.moviesite.RankingGenreBoard.dao;

import com.koreait.moviesite.RankingGenreBoard.dto.BoxOfficeSummary;
import com.koreait.moviesite.RankingGenreBoard.entity.BoxOfficeAlltime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoxOfficeAlltimeRepository extends JpaRepository<BoxOfficeAlltime, Integer> {
    // Sort는 Pageable로 넘길 것이므로 조건 없이 전체에서 꺼내도록
    List<BoxOfficeSummary> findAllBy(Pageable pageable);
}
