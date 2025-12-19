package com.koreait.moviesite.RankingGenreBoard.service;

import com.koreait.moviesite.RankingGenreBoard.dao.BoxOfficeAlltimeRepository;
import com.koreait.moviesite.RankingGenreBoard.dto.BoxOfficeSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AlltimeService {
    private final BoxOfficeAlltimeRepository repo;    

    public Page<BoxOfficeSummary> page(int page, int size, String sortBy, String dir) {
        int p = Math.max(page, 1) - 1;            // 1-based → 0-based
        int s = Math.min(Math.max(size, 1), 100); // 1~100 가드

        String field = switch (sortBy == null ? "" : sortBy.toLowerCase()) {
            case "sales"  -> "salesAcc";
            case "open"   -> "openDt";
            case "movie"  -> "movieNm";
            case "screen" -> "screenCnt";
            default       -> "audiAcc"; // 기본: 관객수
        };
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(p, s, Sort.by(direction, field));
        return repo.findAllProjectedBy(pageable);
    }
}
