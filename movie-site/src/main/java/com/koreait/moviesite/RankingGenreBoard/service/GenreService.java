package com.koreait.moviesite.RankingGenreBoard.service;

import com.koreait.moviesite.RankingGenreBoard.dao.BoxOfficeAlltimeRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GenreService {

    private final BoxOfficeAlltimeRepository repo;
<<<<<<< HEAD

    // ✅ Lombok 없이 생성자 주입
    public GenreService(BoxOfficeAlltimeRepository repo) {
        this.repo = repo;
    }
=======
>>>>>>> branch 'practice' of https://github.com/soossang/koreait-teamproject.git

    public List<String> titles(String genre, int limit, String dir) {
        String g = (genre == null) ? "" : genre.trim();

        int size = Math.min(Math.max(limit, 1), 200);
        Sort.Direction d = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(0, size, Sort.by(d, "movieNm"));

        // ✅ 전체면 필터 없이 전체 영화 제목 조회
        if (g.isEmpty() || "전체".equalsIgnoreCase(g) || "all".equalsIgnoreCase(g)) {
            return repo.findAllTitles(pageable).getContent();
        }

        // 그 외에는 기존 장르 필터
        return repo.findTitlesByGenre(g, pageable).getContent();
    }

    public Page<String> titlesPage(String genre, int page, int size, String dir) {
        String g = (genre == null) ? "" : genre.trim();

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 200); // 안전장치(20 쓰면 됨)

        Sort.Direction d = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(d, "movieNm"));

        // ✅ "전체"면 필터 없이 전체 제목 조회
        if (g.isEmpty() || "전체".equalsIgnoreCase(g) || "all".equalsIgnoreCase(g)) {
            return repo.findAllTitles(pageable);
        }
        return repo.findTitlesByGenre(g, pageable);
    }
}
