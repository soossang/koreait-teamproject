package com.koreait.moviesite.DetailpageReserve.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Screening {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Movie movie;              // 어떤 영화인지

    private String theaterName;       // 상영관 이름 (예: 강남 1관)
    private LocalDateTime screeningTime; // 상영 시간
    private int totalSeats;           // 총 좌석 수
    private int reservedSeats;        // 이미 예매된 좌석 수

    protected Screening() {}

    public Screening(Movie movie, String theaterName,
                     LocalDateTime screeningTime, int totalSeats) {
        this.movie = movie;
        this.theaterName = theaterName;
        this.screeningTime = screeningTime;
        this.totalSeats = totalSeats;
        this.reservedSeats = 0;
    }

    // 잔여 좌석 계산
    public int getAvailableSeats() {
        return totalSeats - reservedSeats;
    }

    // === getter / setter ===
    public Long getId() { return id; }

    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }

    public String getTheaterName() { return theaterName; }
    public void setTheaterName(String theaterName) { this.theaterName = theaterName; }

    public LocalDateTime getScreeningTime() { return screeningTime; }
    public void setScreeningTime(LocalDateTime screeningTime) { this.screeningTime = screeningTime; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public int getReservedSeats() { return reservedSeats; }
    public void setReservedSeats(int reservedSeats) { this.reservedSeats = reservedSeats; }
}
