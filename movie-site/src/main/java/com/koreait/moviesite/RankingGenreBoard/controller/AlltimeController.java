package com.koreait.moviesite.RankingGenreBoard.controller;

import com.koreait.moviesite.RankingGenreBoard.dto.BoxOfficeSummary;
import com.koreait.moviesite.RankingGenreBoard.service.AlltimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/boxoffice")
public class AlltimeController {

    private final AlltimeService service;
    
    public AlltimeController(AlltimeService service) {
    	this.service = service;
    }
    
    

    // 기존: /api/boxoffice/alltime?limit=...&sortBy=...&dir=...
    // public ResponseEntity<List<BoxOfficeSummary>> alltime(...)

    // ★ 새로 추가: 페이지네이션 버전
    // 예) /api/boxoffice/alltime-page?page=1&size=30&sortBy=audi&dir=desc
    @GetMapping("/alltime-page")
    public ResponseEntity<Page<BoxOfficeSummary>> alltimePage(
            @RequestParam(name="page",  defaultValue="1")  int page,
            @RequestParam(name="size",  defaultValue="30") int size,
            @RequestParam(name="sortBy",defaultValue="audi") String sortBy,
            @RequestParam(name="dir",   defaultValue="desc") String dir) {

        return ResponseEntity.ok(service.page(page, size, sortBy, dir));
    }
}
