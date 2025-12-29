package com.koreait.moviesite.DetailpageReserve.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

@Entity
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;        // 영화 제목
    private String genre;        // 장르
    private String runningTime;  // 상영 시간(분)
    private String rating;       // 관람등급 (전체, 12, 15, 19 등)

    @Column(length = 2000)
    private String description;  // 줄거리

    private String posterUrl;    // 포스터 이미지 경로

    // 예고편 / 관련 영상 ID 목록 (콤마로 구분된 YouTube 영상 ID 문자열)
    // 예: "AAA111,BBB222,CCC333"
    @Column(name = "trailer_urls", length = 1000)
    private String trailerUrls;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Screening> screenings = new ArrayList<>();

    protected Movie() {}

    public Movie(String title, String genre, String runningTime, String rating,
                 String description, String posterUrl) {
        this.title = title;
        this.genre = genre;
        this.runningTime = runningTime;
        this.rating = rating;
        this.description = description;
        this.posterUrl = posterUrl;
    }

    // === getter / setter ===
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(String runningTime) {
        this.runningTime = runningTime;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public List<Screening> getScreenings() {
        return screenings;
    }

    public String getTrailerUrls() {
        return trailerUrls;
    }

    public void setTrailerUrls(String trailerUrls) {
        this.trailerUrls = trailerUrls;
    }

    /**
     * DB 에 "AAA111,BBB222,CCC333" 처럼 저장된 문자열을
     * List<String> 으로 변환해서 사용하기 위한 편의 메서드
     */
    public List<String> getTrailerIdList() {
        if (trailerUrls == null || trailerUrls.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(trailerUrls.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
