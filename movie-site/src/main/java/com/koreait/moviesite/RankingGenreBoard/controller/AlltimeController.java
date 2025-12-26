package com.koreait.moviesite.RankingGenreBoard.controller;

import com.koreait.moviesite.RankingGenreBoard.dto.BoxOfficeSummary;
import com.koreait.moviesite.RankingGenreBoard.service.AlltimeService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/boxoffice")
public class AlltimeController {

    private final AlltimeService service;

    // ✅ Lombok 없어도 되는 생성자 주입
    public AlltimeController(AlltimeService service) {
        this.service = service;
    }

    /**
     * 역대 박스오피스 페이지 조회 (Page<BoxOfficeSummary>)
     *
     * 예)
     *  /api/boxoffice/alltime-page?page=1&size=30&sortBy=audi&dir=desc
     *
     * sortBy 후보(서비스 로직 기준):
     *  - audi   : 누적 관객수(audiAcc)  [기본]
     *  - sales  : 누적 매출액(salesAcc)
     *  - open   : 개봉일(openDt)
     *  - movie  : 영화명(movieNm)
     *  - screen : 스크린수(screenCnt)
     *
     * dir 후보:
     *  - asc / desc (기본 desc)
     *
     * ※ page는 1부터 시작(1-based). service에서 0-based로 변환 처리함.
     */
    @GetMapping("/alltime-page")
    public ResponseEntity<Page<BoxOfficeSummary>> alltimePage(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "30") int size,
            @RequestParam(name = "sortBy", defaultValue = "audi") String sortBy,
            @RequestParam(name = "dir", defaultValue = "desc") String dir
    ) {
        Page<BoxOfficeSummary> result = service.page(page, size, sortBy, dir);
        return ResponseEntity.ok(result);
    }
}
