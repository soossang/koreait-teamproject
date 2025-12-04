package com.koreait.moviesite.Member.dao;

import com.koreait.moviesite.Member.entity.MemberEntity;
import com.koreait.moviesite.Member.entity.MovieRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRatingRepository extends JpaRepository<MovieRating, Long> {

    Page<MovieRating> findByMember(MemberEntity member, Pageable pageable);

    MovieRating findByMember_IdAndMovie_Id(Long memberId, Long movieId);
}
