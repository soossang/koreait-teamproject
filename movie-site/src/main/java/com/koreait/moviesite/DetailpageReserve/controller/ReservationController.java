package com.koreait.moviesite.DetailpageReserve.controller;

import com.koreait.movie_project.domain.Reservation;
import com.koreait.movie_project.domain.Screening;
import com.koreait.movie_project.repository.ReservationRepository;
import com.koreait.movie_project.repository.ScreeningRepository;
import com.koreait.movie_project.service.ReservationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/reserve")
public class ReservationController {

    private final ScreeningRepository screeningRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    public ReservationController(ScreeningRepository screeningRepository,
                                 ReservationRepository reservationRepository,
                                 ReservationService reservationService) {
        this.screeningRepository = screeningRepository;
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
    }

    /**
     * 좌석 선택 + 예매 폼
     */
    @GetMapping("/{screeningId}")
    public String reserveForm(@PathVariable("screeningId") Long screeningId, Model model) {
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new IllegalArgumentException("상영 정보를 찾을 수 없습니다. id=" + screeningId));

        List<String> rows = Arrays.asList("A", "B", "C", "D", "E");
        List<Integer> cols = IntStream.rangeClosed(1, 10).boxed().toList();

        List<Reservation> reservations = reservationRepository.findByScreeningId(screeningId);
        Set<String> reservedSeats = reservations.stream()
                .filter(r -> r.getSeats() != null)
                .flatMap(r -> Arrays.stream(r.getSeats().split(",")))
                .map(String::trim)
                .collect(Collectors.toSet());

        int availableSeats = reservationService.getAvailableSeats(screeningId);

        model.addAttribute("screening", screening);
        model.addAttribute("rows", rows);
        model.addAttribute("cols", cols);
        model.addAttribute("reservedSeats", reservedSeats);
        model.addAttribute("availableSeats", availableSeats);

        return "reservation/form";
    }


    /**
     * 예매(결제) 처리
     */
    @PostMapping("/{screeningId}")
    public String reserve(@PathVariable("screeningId") Long screeningId,
                          @RequestParam("name") String name,
                          @RequestParam("phone") String phone,
                          @RequestParam("count") int count,
                          @RequestParam("selectedSeats") String selectedSeats,
                          Model model) {

        Reservation reservation =
                reservationService.reserve(screeningId, name, phone, count, selectedSeats);

        model.addAttribute("reservation", reservation);
        return "reservation/complete";
    }
}
