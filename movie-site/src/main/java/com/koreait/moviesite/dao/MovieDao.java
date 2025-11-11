package com.koreait.moviesite.dao;

import com.koreait.moviesite.entity.MovieEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieDao extends JpaRepository<MovieEntity, Long> {
    // 기본 CRUD는 JpaRepository가 다 제공
}
