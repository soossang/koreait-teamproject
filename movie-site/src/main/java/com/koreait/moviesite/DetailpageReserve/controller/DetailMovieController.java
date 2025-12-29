package com.koreait.moviesite.DetailpageReserve.controller;

import com.koreait.moviesite.DetailpageReserve.domain.Movie;
import com.koreait.moviesite.DetailpageReserve.domain.Screening;
import com.koreait.moviesite.DetailpageReserve.repository.ScreeningRepository;
import com.koreait.moviesite.DetailpageReserve.service.MovieService;
import com.koreait.moviesite.DetailpageReserve.service.ReservationService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/movies")
public class DetailMovieController {

    private final MovieService movieService;
    private final ScreeningRepository screeningRepository;
    private final ReservationService reservationService;

    public DetailMovieController(MovieService movieService,
                           ScreeningRepository screeningRepository,
                           ReservationService reservationService) {
        this.movieService = movieService;
        this.screeningRepository = screeningRepository;
        this.reservationService = reservationService;
    }

    // ★ 홈(목록) 화면: GET /movies
    @GetMapping
    public String list(Model model) {
        List<Movie> movies = movieService.findAll();
        model.addAttribute("movies", movies);
        return "DetailpageReserve/movies/list";    // templates/movies/list.html
    }

    // ★ 상세 화면: GET /movies/{id}
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Movie movie = movieService.findById(id);

        // 이 영화의 상영정보 목록
        List<Screening> screenings =
                screeningRepository.findByMovieIdOrderByScreeningTimeAsc(id);

        // 각 상영정보별 잔여 좌석 계산 (ReservationService 사용)
        Map<Long, Integer> availableSeatsMap = screenings.stream()
                .collect(Collectors.toMap(
                        Screening::getId,
                        s -> reservationService.getAvailableSeats(s.getId())
                ));

        // 이 영화의 예고편 / 관련 영상 YouTube ID 리스트
        List<String> trailerIds = movie.getTrailerIdList();
        model.addAttribute("movie", movie);
        model.addAttribute("screenings", screenings);
        model.addAttribute("availableSeatsMap", availableSeatsMap);
        model.addAttribute("trailerIds", trailerIds);

        return "DetailpageReserve/movies/detail";
    }
}
