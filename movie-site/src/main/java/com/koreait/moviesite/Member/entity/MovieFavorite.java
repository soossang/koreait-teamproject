package com.koreait.moviesite.Member.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "movie_favorite")
public class MovieFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    // 영화 (★ MemberMovieEntity)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private MemberMovieEntity movie;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public MovieFavorite() {}

    // ===== getter / setter =====
    public Long getId() { return id; }

    public MemberEntity getMember() { return member; }

    public void setMember(MemberEntity member) { this.member = member; }

    public MemberMovieEntity getMovie() { return movie; }

    public void setMovie(MemberMovieEntity movie) { this.movie = movie; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
