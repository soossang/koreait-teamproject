package com.koreait.moviesite.RankingGenreboard.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "movie")
public class MovieEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=200)
    private String title;

    @Column(length=100)
    private String director;

    @Column
    private int year;

    @Column(length=50)
    private String genre;               // 예: "Action", "Drama" ...

    @Column
    private Long boxOfficeGross;        // 흥행 매출(정렬용)

    // getter/setter, 기본 생성자
    public MovieEntity() {}
    public MovieEntity(Long id, String title, String director, int year, String genre, Long boxOfficeGross) {
        this.id = id; this.title = title; this.director = director; this.year = year; this.genre = genre; this.boxOfficeGross = boxOfficeGross;
    }
    public Long getId() { return id; }      public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }  public void setTitle(String title) { this.title = title; }
    public String getDirector() { return director; } public void setDirector(String director) { this.director = director; }
    public int getYear() { return year; }   public void setYear(int year) { this.year = year; }
    public String getGenre() { return genre; } public void setGenre(String genre) { this.genre = genre; }
    public Long getBoxOfficeGross() { return boxOfficeGross; } public void setBoxOfficeGross(Long v) { this.boxOfficeGross = v; }
}
