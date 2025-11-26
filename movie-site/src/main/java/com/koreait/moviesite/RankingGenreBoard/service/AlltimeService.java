package com.koreait.moviesite.RankingGenreBoard.service;

import com.koreait.moviesite.RankingGenreBoard.dao.BoxOfficeAlltimeRepository;
import com.koreait.moviesite.RankingGenreBoard.dto.BoxOfficeSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AlltimeService {
    private final BoxOfficeAlltimeRepository repo;

    public List<BoxOfficeSummary> topN(int limit, String sortBy, String dir) {
        int size = Math.min(Math.max(limit, 1), 100);

        // 허용된 정렬 키 매핑
        String field = switch (sortBy == null ? "" : sortBy.toLowerCase()) {
            case "sales"  -> "salesAcc";
            case "open"   -> "openDt";
            case "movie"  -> "movieNm";
            case "screen" -> "screenCnt";
            default       -> "audiAcc"; // audi(기본)
        };

        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return repo.findAllBy(PageRequest.of(0, size, Sort.by(direction, field)));
    }
}
