package com.koreait.moviesite.Member.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "movie_rating")
public class MovieRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    // 영화 (★ 여기 타입이 MemberMovieEntity)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private MemberMovieEntity movie;

    @Column(nullable = false)
    private int rating;

    @Column(columnDefinition = "TEXT")
    private String review;

    private LocalDate watchedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public MovieRating() {}

    // ===== getter / setter =====
    public Long getId() { return id; }

    public MemberEntity getMember() { return member; }

    public void setMember(MemberEntity member) { this.member = member; }

    public MemberMovieEntity getMovie() { return movie; }

    public void setMovie(MemberMovieEntity movie) { this.movie = movie; }

    public int getRating() { return rating; }

    public void setRating(int rating) { this.rating = rating; }

    public String getReview() { return review; }

    public void setReview(String review) { this.review = review; }

    public LocalDate getWatchedAt() { return watchedAt; }

    public void setWatchedAt(LocalDate watchedAt) { this.watchedAt = watchedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
