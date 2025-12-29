package com.koreait.moviesite.Member.dao;

import com.koreait.moviesite.Member.entity.MemberMovieEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberMovieRepository extends JpaRepository<MemberMovieEntity, Long> {
}
