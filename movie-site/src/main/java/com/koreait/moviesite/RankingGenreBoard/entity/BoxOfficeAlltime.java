package com.koreait.moviesite.RankingGenreBoard.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder @ToString @EqualsAndHashCode(of = "rankNo")
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

    @Column(name = "audi_acc", nullable = false)
    private Long audiAcc;

    @Column(name = "screen_cnt")
    private Integer screenCnt;

    // DB DEFAULT CURRENT_TIMESTAMP 사용
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
