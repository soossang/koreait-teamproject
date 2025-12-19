package com.koreait.moviesite.RankingGenreBoard.controller;

import com.koreait.moviesite.RankingGenreBoard.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class RankingMovieController {

    private final GenreService genreService;

    // ✅ 한 페이지에 20개 고정
    private static final int PAGE_SIZE = 20;

    // 예:
    // /api/movies?genre=액션&dir=asc&page=0
    // /api/movies?genre=전체&dir=desc&page=3
    @GetMapping("/movies")
    public ResponseEntity<TitlesPageResponse> movies(
            @RequestParam(name = "genre") String genre,
            @RequestParam(name = "dir", defaultValue = "asc") String dir,
            @RequestParam(name = "page", defaultValue = "0") int page,
            // ✅ 기존 JS나 다른 호출에서 sortBy가 붙어도 깨지지 않게 받기만 함(사용 안 함)
            @RequestParam(name = "sortBy", required = false) String sortBy
    ) {
        int safePage = Math.max(page, 0);

        // ✅ GenreService에서 Page<String>으로 가져오기
        Page<String> p = genreService.titlesPage(genre, safePage, PAGE_SIZE, dir);

        List<MovieTitleDto> content = p.getContent()
                .stream()
                .map(MovieTitleDto::new)
                .toList();

        TitlesPageResponse body = new TitlesPageResponse(
                content,
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages()
        );

        return ResponseEntity.ok(body);
    }

    // ===== 응답 DTO =====
    public record MovieTitleDto(String movieNm) {}
    public record TitlesPageResponse(
            List<MovieTitleDto> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}
}
