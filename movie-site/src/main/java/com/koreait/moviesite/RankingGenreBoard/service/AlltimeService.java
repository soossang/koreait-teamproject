package com.koreait.moviesite.RankingGenreBoard.service;

import com.koreait.moviesite.RankingGenreBoard.dao.BoxOfficeAlltimeRepository;
import com.koreait.moviesite.RankingGenreBoard.dto.BoxOfficeSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AlltimeService {

    private final BoxOfficeAlltimeRepository repo;

    // ✅ 생성자 주입 (Lombok 없어도 OK)
    public AlltimeService(BoxOfficeAlltimeRepository repo) {
        this.repo = repo;
    }

    /**
     * page: 1부터 시작(1-based) -> 내부에서 0-based로 변환
     * size: 1~100 제한
     * sortBy: audi(기본), sales, open, movie, screen
     * dir: asc / desc (기본 desc)
     */
    public Page<BoxOfficeSummary> page(int page, int size, String sortBy, String dir) {
        int p = Math.max(page, 1) - 1;            // 1-based → 0-based
        int s = Math.min(Math.max(size, 1), 100); // 1~100 가드

        String field = "audiAcc"; // 기본: 관객수
        String sb = (sortBy == null) ? "" : sortBy.trim().toLowerCase();

        switch (sb) {
            case "sales":
                field = "salesAcc";
                break;
            case "open":
                field = "openDt";
                break;
            case "movie":
                field = "movieNm";
                break;
            case "screen":
                field = "screenCnt";
                break;
            case "audi":
            default:
                field = "audiAcc";
                break;
        }

        Sort.Direction direction =
                "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(p, s, Sort.by(direction, field));
        return repo.findAllProjectedBy(pageable);
    }
}
