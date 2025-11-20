package com.koreait.moviesite.service;

import com.koreait.moviesite.dto.MovieDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MovieService {
    List<MovieDto> topBoxOffice(int limit);
    List<String> genres();
    Page<MovieDto> byGenre(String genre, Pageable pageable);

    // ★ 추가: 장르 없을 때 전체 조회
    Page<MovieDto> all(Pageable pageable);
}
