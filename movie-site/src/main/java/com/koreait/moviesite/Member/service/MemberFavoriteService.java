package com.koreait.moviesite.Member.service;

import com.koreait.moviesite.Member.dao.MemberMovieRepository;
import com.koreait.moviesite.Member.dao.MemberRepository;
import com.koreait.moviesite.Member.dao.MovieFavoriteRepository;
import com.koreait.moviesite.Member.dto.MovieFavoriteResponse;
import com.koreait.moviesite.Member.entity.MemberEntity;
import com.koreait.moviesite.Member.entity.MemberMovieEntity;
import com.koreait.moviesite.Member.entity.MovieFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MemberFavoriteService {

    private final MemberRepository memberRepository;
    private final MovieFavoriteRepository movieFavoriteRepository;
    private final MemberMovieRepository memberMovieRepository;

    public MemberFavoriteService(MemberRepository memberRepository,
                                 MovieFavoriteRepository movieFavoriteRepository,
                                 MemberMovieRepository memberMovieRepository) {
        this.memberRepository = memberRepository;
        this.movieFavoriteRepository = movieFavoriteRepository;
        this.memberMovieRepository = memberMovieRepository;
    }

    public Page<MovieFavoriteResponse> getMyFavorites(Long memberId, Pageable pageable) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        return movieFavoriteRepository.findByMember(member, pageable)
                .map(this::toResponse);
    }

    public void addFavorite(Long memberId, Long movieId) {
        if (movieFavoriteRepository.existsByMember_IdAndMovie_Id(memberId, movieId)) return;

        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        MemberMovieEntity movie = memberMovieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("영화 정보를 찾을 수 없습니다."));

        MovieFavorite favorite = new MovieFavorite();
        favorite.setMember(member);
        favorite.setMovie(movie);
        movieFavoriteRepository.save(favorite);
    }

    public void removeFavorite(Long memberId, Long movieId) {
        movieFavoriteRepository.deleteByMember_IdAndMovie_Id(memberId, movieId);
    }

    private MovieFavoriteResponse toResponse(MovieFavorite f) {
        MemberMovieEntity m = f.getMovie();
        return new MovieFavoriteResponse(
                m.getId(),
                m.getTitle(),
                m.getDirector(),
                m.getYear(),
                m.getGenre(),
                m.getBoxOfficeGross(),
                f.getCreatedAt()
        );
    }
}
