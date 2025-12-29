package com.koreait.moviesite.Member.dao;

import com.koreait.moviesite.Member.entity.MemberEntity;
import com.koreait.moviesite.Member.entity.MovieFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieFavoriteRepository extends JpaRepository<MovieFavorite, Long> {

    Page<MovieFavorite> findByMember(MemberEntity member, Pageable pageable);

    boolean existsByMember_IdAndMovie_Id(Long memberId, Long movieId);

    void deleteByMember_IdAndMovie_Id(Long memberId, Long movieId);
}
