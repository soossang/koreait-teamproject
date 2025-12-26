package com.koreait.moviesite.Common.controller;

import com.koreait.moviesite.DetailpageReserve.domain.Movie;
import com.koreait.moviesite.DetailpageReserve.repository.MovieRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/home")
public class HomeMovieApiController {

    private final MovieRepository movieRepository;

    public HomeMovieApiController(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    // 홈 상단 대표 영화: 기본은 id=1(명량)
    @GetMapping("/featured")
    public ResponseEntity<MovieDto> featured(
            @RequestParam(name = "id", defaultValue = "1") Long id
    ) {
        Movie m = movieRepository.findById(id).orElse(null);
        if (m == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(MovieDto.from(m));
    }

    // 박스오피스 영역: 기본은 id=2,3,4,5,6 순서
    @GetMapping("/boxoffice")
    public List<MovieDto> boxoffice(
            @RequestParam(name = "ids", required = false) List<Long> ids
    ) {
        List<Long> order = (ids == null || ids.isEmpty())
                ? Arrays.asList(2L, 3L, 4L, 5L, 6L)
                : ids;

        // findAllById는 순서를 보장하지 않으므로 map으로 정렬
        Map<Long, Movie> map = new HashMap<>();
        for (Movie m : movieRepository.findAllById(order)) {
            map.put(m.getId(), m);
        }

        List<MovieDto> out = new ArrayList<>();
        for (Long id : order) {
            Movie m = map.get(id);
            if (m != null) out.add(MovieDto.from(m));
        }
        return out;
    }

    public record MovieDto(
            Long id,
            String title,
            String genre,
            String rating,
            String posterUrl
    ) {
        public static MovieDto from(Movie m) {
            return new MovieDto(
                    m.getId(),
                    m.getTitle(),
                    m.getGenre(),
                    m.getRating(),
                    m.getPosterUrl()
            );
        }
    }
}
