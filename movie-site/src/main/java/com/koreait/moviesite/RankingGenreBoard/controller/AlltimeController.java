package com.koreait.moviesite.RankingGenreBoard.controller;

import com.koreait.moviesite.RankingGenreBoard.dto.BoxOfficeSummary;
import com.koreait.moviesite.RankingGenreBoard.service.AlltimeService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boxoffice")
public class AlltimeController {

    private final AlltimeService service;

<<<<<<< HEAD
    // ✅ Lombok 없이 생성자 주입
    public AlltimeController(AlltimeService service) {
        this.service = service;
    }

    // 페이지네이션 버전
    // 예) /api/boxoffice/alltime-page?page=1&size=30&sortBy=audi&dir=desc
=======
>>>>>>> branch 'practice' of https://github.com/soossang/koreait-teamproject.git
    @GetMapping("/alltime-page")
    public ResponseEntity<Page<BoxOfficeSummary>> alltimePage(
<<<<<<< HEAD
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "30") int size,
            @RequestParam(name = "sortBy", defaultValue = "audi") String sortBy,
            @RequestParam(name = "dir", defaultValue = "desc") String dir
=======
            @RequestParam(value="page", defaultValue="1") int page,
            @RequestParam(value="size", defaultValue="30") int size,
            @RequestParam(value="sortBy", defaultValue="audi") String sortBy,
            @RequestParam(value="dir", defaultValue="desc") String dir
>>>>>>> branch 'practice' of https://github.com/soossang/koreait-teamproject.git
    ) {
        return ResponseEntity.ok(service.page(page, size, sortBy, dir));
    }
}
