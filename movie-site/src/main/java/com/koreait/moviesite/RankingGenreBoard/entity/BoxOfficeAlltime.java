package com.koreait.moviesite.RankingGenreBoard.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
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
    @Builder.Default
    private Set<Genre> genres = new LinkedHashSet<>();
}
