package com.koreait.moviesite.DetailpageReserve.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Reservation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Screening screening;

    private String name;       // 예매자 이름
    private String phone;      // 연락처
    private int reservedCount; // 인원 수

    // 예매 조회/수정에 사용하는 10자리 랜덤 코드 (DB: reservation_number)
    @Column(name = "reservation_number", nullable = false, unique = true, length = 10)
    private String reservationNumber;

    // 새로 추가: 좌석 목록 (예: "A1,A2,A3")
    private String seats;

    private LocalDateTime reservedAt;

    protected Reservation() {}

    public Reservation(Screening screening, String reservationNumber, String name,
                       String phone, int reservedCount, String seats) {
        this.screening = screening;
        this.reservationNumber = reservationNumber;
        this.name = name;
        this.phone = phone;
        this.reservedCount = reservedCount;
        this.seats = seats;
        this.reservedAt = LocalDateTime.now();
    }

    // === getter ===
    public Long getId() { return id; }
    public Screening getScreening() { return screening; }
    public String getReservationNumber() { return reservationNumber; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public int getReservedCount() { return reservedCount; }
    public String getSeats() { return seats; }
    public LocalDateTime getReservedAt() { return reservedAt; }

    // === update ===
    public void updateInfo(String name, String phone, int reservedCount, String seats) {
        this.name = name;
        this.phone = phone;
        this.reservedCount = reservedCount;
        this.seats = seats;
    }
}
