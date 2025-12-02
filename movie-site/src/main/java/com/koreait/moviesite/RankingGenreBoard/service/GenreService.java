package com.koreait.moviesite.RankingGenreBoard.service;

import com.koreait.moviesite.RankingGenreBoard.dao.BoxOfficeAlltimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class GenreService {
    private final BoxOfficeAlltimeRepository repo;

    public List<String> titles(String genre, int limit, String dir) {
        int size = Math.min(Math.max(limit, 1), 200);
        Sort.Direction d = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(0, size, Sort.by(d, "movieNm"));
        return repo.findTitlesByGenre(genre, pageable).getContent();
    }
}
