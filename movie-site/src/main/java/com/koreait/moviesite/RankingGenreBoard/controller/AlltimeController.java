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

    @GetMapping("/alltime-page")
    public ResponseEntity<Page<BoxOfficeSummary>> alltimePage(
            @RequestParam(value="page", defaultValue="1") int page,
            @RequestParam(value="size", defaultValue="30") int size,
            @RequestParam(value="sortBy", defaultValue="audi") String sortBy,
            @RequestParam(value="dir", defaultValue="desc") String dir) {

        return ResponseEntity.ok(service.page(page, size, sortBy, dir));
    }
}
