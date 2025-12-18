package com.koreait.moviesite.RankingGenreBoard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RankingPageController {

    // 실제 흥행영화순위 페이지
    @GetMapping("/ranking")
    public String ranking() {
        return "RankingGenreboard/ranking"; // ✅ ranking.html로 매핑
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> branch 'practice' of https://github.com/soossang/koreait-teamproject.git
