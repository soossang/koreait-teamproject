package com.koreait.moviesite.Common.controller;

import com.koreait.moviesite.DetailpageReserve.domain.Movie;
import com.koreait.moviesite.DetailpageReserve.repository.MovieRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Controller
public class RootController {

    private final MovieRepository movieRepository;

    public RootController(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @GetMapping("/")
    public String root(Model model) {

        // ✅ 메인 대표 영화: id=1 (명량)
        Movie featured = movieRepository.findById(1L).orElse(null);
        model.addAttribute("featured", featured);

        // ✅ 박스오피스: id=2,3,4,5,6 (순서 유지)
        List<Long> order = Arrays.asList(2L, 3L, 4L, 5L, 6L);

        Map<Long, Movie> map = new HashMap<>();
        for (Movie m : movieRepository.findAllById(order)) {
            map.put(m.getId(), m);
        }

        List<Movie> boxoffice = new ArrayList<>();
        for (Long id : order) {
            Movie m = map.get(id);
            if (m != null) boxoffice.add(m);
        }
        model.addAttribute("boxoffice", boxoffice);

        return "Member/index";
    }
}
