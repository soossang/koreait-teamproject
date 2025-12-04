package com.koreait.moviesite.Member.service;

import com.koreait.moviesite.Member.dao.MemberMovieRepository;
import com.koreait.moviesite.Member.dao.MemberRepository;
import com.koreait.moviesite.Member.dao.MovieRatingRepository;
import com.koreait.moviesite.Member.dto.MovieRatingRequest;
import com.koreait.moviesite.Member.dto.MovieRatingResponse;
import com.koreait.moviesite.Member.entity.MemberEntity;
import com.koreait.moviesite.Member.entity.MemberMovieEntity;
import com.koreait.moviesite.Member.entity.MovieRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MemberRatingService {

    private final MemberRepository memberRepository;
    private final MovieRatingRepository movieRatingRepository;
    private final MemberMovieRepository memberMovieRepository;

    public MemberRatingService(MemberRepository memberRepository,
                               MovieRatingRepository movieRatingRepository,
                               MemberMovieRepository memberMovieRepository) {
        this.memberRepository = memberRepository;
        this.movieRatingRepository = movieRatingRepository;
        this.memberMovieRepository = memberMovieRepository;
    }

    public Page<MovieRatingResponse> getMyRatings(Long memberId, Pageable pageable) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        return movieRatingRepository.findByMember(member, pageable)
                .map(this::toResponse);
    }

    public MovieRatingResponse rateMovie(Long memberId, Long movieId, MovieRatingRequest request) {
        if (request.rating() < 1 || request.rating() > 5) {
            throw new IllegalArgumentException("별점은 1~5 사이여야 합니다.");
        }

        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        MemberMovieEntity movie = memberMovieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("영화 정보를 찾을 수 없습니다."));

        MovieRating rating = movieRatingRepository.findByMember_IdAndMovie_Id(memberId, movieId);
        if (rating == null) {
            rating = new MovieRating();
            rating.setMember(member);
            rating.setMovie(movie);
        }

        rating.setRating(request.rating());
        rating.setReview(request.review());
        rating.setWatchedAt(request.watchedAt());

        MovieRating saved = movieRatingRepository.save(rating);
        return toResponse(saved);
    }

    private MovieRatingResponse toResponse(MovieRating r) {
        MemberMovieEntity m = r.getMovie();
        return new MovieRatingResponse(
                m.getId(),
                m.getTitle(),
                r.getRating(),
                r.getReview(),
                r.getWatchedAt(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
