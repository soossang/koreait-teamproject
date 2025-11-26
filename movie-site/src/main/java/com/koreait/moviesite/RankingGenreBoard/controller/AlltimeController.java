package com.koreait.moviesite.RankingGenreBoard.controller;

import com.koreait.moviesite.RankingGenreBoard.dto.BoxOfficeSummary;
import com.koreait.moviesite.RankingGenreBoard.service.AlltimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/boxoffice")
public class AlltimeController {

    private final AlltimeService service;

    // 예: /api/boxoffice/alltime?limit=10&sortBy=audi|sales|open|movie|screen&dir=asc|desc
    @GetMapping("/alltime")
    public ResponseEntity<List<BoxOfficeSummary>> alltime(
            @RequestParam(name="limit",  defaultValue="10") int limit,
            @RequestParam(name="sortBy", defaultValue="audi") String sortBy,
            @RequestParam(name="dir",    defaultValue="desc") String dir) {

        return ResponseEntity.ok(service.topN(limit, sortBy, dir));
    }
}
