package com.koreait.moviesite.RankingGenreBoard.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "box_office_alltime")
public class BoxOfficeAlltime {

    @Id
    @Column(name = "rank_no")
    private Integer rankNo;

    @Column(name = "movie_nm", nullable = false, length = 200)
    private String movieNm;

    @Column(name = "open_dt")
    private LocalDate openDt;

    @Column(name = "sales_acc")
    private Long salesAcc;

    @Column(name = "audi_acc")
    private Long audiAcc;

    @Column(name = "screen_cnt")
    private Integer screenCnt;

    // 장르 매핑 (N:M)
    @ManyToMany
    @JoinTable(
            name = "box_office_genre",
            joinColumns = @JoinColumn(name = "rank_no"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new LinkedHashSet<>();

    // ✅ JPA 필수: 기본 생성자
    public BoxOfficeAlltime() {}

    public Integer getRankNo() {
        return rankNo;
    }

    public void setRankNo(Integer rankNo) {
        this.rankNo = rankNo;
    }

    public String getMovieNm() {
        return movieNm;
    }

    public void setMovieNm(String movieNm) {
        this.movieNm = movieNm;
    }

    public LocalDate getOpenDt() {
        return openDt;
    }

    public void setOpenDt(LocalDate openDt) {
        this.openDt = openDt;
    }

    public Long getSalesAcc() {
        return salesAcc;
    }

    public void setSalesAcc(Long salesAcc) {
        this.salesAcc = salesAcc;
    }

    public Long getAudiAcc() {
        return audiAcc;
    }

    public void setAudiAcc(Long audiAcc) {
        this.audiAcc = audiAcc;
    }

    public Integer getScreenCnt() {
        return screenCnt;
    }

    public void setScreenCnt(Integer screenCnt) {
        this.screenCnt = screenCnt;
    }

    public Set<Genre> getGenres() {
        return genres;
    }

    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
    }
}
