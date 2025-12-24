package com.koreait.moviesite.DetailpageReserve.service;

import com.koreait.moviesite.DetailpageReserve.domain.Reservation;
import com.koreait.moviesite.DetailpageReserve.domain.Screening;
import com.koreait.moviesite.DetailpageReserve.repository.ReservationRepository;
import com.koreait.moviesite.DetailpageReserve.repository.ScreeningRepository;
import com.koreait.moviesite.Member.dao.MemberRepository;
import com.koreait.moviesite.Member.entity.MemberEntity;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ScreeningRepository screeningRepository;
    private final MemberRepository memberRepository;

    private static final String RESERVATION_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final SecureRandom secureRandom = new SecureRandom();

    public ReservationService(ReservationRepository reservationRepository,
                              ScreeningRepository screeningRepository,
                              MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.screeningRepository = screeningRepository;
        this.memberRepository = memberRepository;
    }
    
    @Transactional(readOnly = true)
    public int getAvailableSeats(Long screeningId) {
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new IllegalArgumentException("상영 정보를 찾을 수 없습니다. id=" + screeningId));

        int total = screening.getTotalSeats();
        int reserved = reservationRepository.sumReservedCountByScreeningId(screeningId);

        return total - reserved;
    }
    

    // 좌석 포함 예매
    @Transactional
    public Reservation reserve(Long screeningId, String name,
                               String phone, int count, String seats) {

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new IllegalArgumentException("상영 정보를 찾을 수 없습니다. id=" + screeningId));

        // 사용자가 선택한 좌석 목록
        List<String> selectedSeats = Arrays.stream(seats.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (selectedSeats.size() != count) {
            throw new IllegalStateException("선택한 좌석 수와 인원 수가 다릅니다.");
        }

        // 이미 예약된 좌석 목록
        List<Reservation> reservations = reservationRepository.findByScreeningId(screeningId);
        Set<String> reservedSeats = reservations.stream()
                .filter(r -> r.getSeats() != null)
                .flatMap(r -> Arrays.stream(r.getSeats().split(",")))
                .map(String::trim)
                .collect(Collectors.toSet());

        // 중복 좌석 체크
        for (String seat : selectedSeats) {
            if (reservedSeats.contains(seat)) {
                throw new IllegalStateException("이미 예약된 좌석이 포함되어 있습니다: " + seat);
            }
        }

        // 잔여 좌석 확인 (예약 테이블 기준으로 계산)
        int availableSeats = getAvailableSeats(screeningId);
        if (availableSeats < count) {
            throw new IllegalStateException("잔여 좌석이 부족합니다.");
        }

        String reservationNumber = generateUniqueReservationNumber(10);

        String normalizedPhone = normalizePhone(phone);
        Reservation reservation =
                new Reservation(screening, reservationNumber, name, normalizedPhone, count, seats);
        return reservationRepository.save(reservation);
    }

    /**
     * (A + B) 로그인한 회원이 예매할 때는 예매 정보를 member_id로 연결하고,
     * 연락처는 회원 정보의 phone으로 강제(가능하면)하여 "아이디별 예매 조회"가 안정적으로 되게 한다.
     */
    @Transactional
    public Reservation reserveForMember(Long memberId,
                                        Long screeningId,
                                        String name,
                                        String phone,
                                        int count,
                                        String seats) {

        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다. id=" + memberId));

        // 회원 정보의 phone이 있으면 그 값을 우선 사용(강제)
        String memberPhone = normalizePhone(member.getPhone());
        String finalPhone = (memberPhone != null && !memberPhone.isBlank())
                ? memberPhone
                : normalizePhone(phone);

        // 이름은 입력값이 있으면 사용, 없으면 회원 이름으로 fallback
        String finalName = (name != null && !name.isBlank()) ? name : member.getName();

        Reservation reservation = reserve(screeningId, finalName, finalPhone, count, seats);
        reservation.assignMember(member);
        return reservation;
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAll(Sort sort) {
        return reservationRepository.findAll(sort);
    }

@Transactional(readOnly = true)
    public Reservation getByReservationNumber(String reservationNumber) {
        return reservationRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new IllegalArgumentException("예매 정보를 찾을 수 없습니다. 예매번호=" + reservationNumber));
    }

    @Transactional
    public Reservation updateByReservationNumber(String reservationNumber,
                                                String name,
                                                String phone,
                                                int reservedCount,
                                                String seats) {

        Reservation reservation = getByReservationNumber(reservationNumber);
        Long screeningId = reservation.getScreening().getId();

        // 사용자가 선택한 좌석 목록
        List<String> selectedSeats = Arrays.stream(seats.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (selectedSeats.size() != reservedCount) {
            throw new IllegalStateException("선택한 좌석 수와 인원 수가 다릅니다.");
        }

        // 이미 예약된 좌석 목록(본인 예약 제외)
        List<Reservation> reservations = reservationRepository.findByScreeningId(screeningId);
        Set<String> reservedSeats = reservations.stream()
                .filter(r -> !Objects.equals(r.getId(), reservation.getId()))
                .filter(r -> r.getSeats() != null)
                .flatMap(r -> Arrays.stream(r.getSeats().split(",")))
                .map(String::trim)
                .collect(Collectors.toSet());

        for (String seat : selectedSeats) {
            if (reservedSeats.contains(seat)) {
                throw new IllegalStateException("이미 예약된 좌석이 포함되어 있습니다: " + seat);
            }
        }

        // (A) 연락처는 저장/비교 안정성을 위해 항상 정규화
        String normalizedPhone = normalizePhone(phone);

        // (B) member로 연결된 예매는 회원 phone으로 강제 (계정별 조회 일관성 유지)
        if (reservation.getMember() != null) {
            normalizedPhone = normalizePhone(reservation.getMember().getPhone());
        }

        reservation.updateInfo(name, normalizedPhone, reservedCount, seats);
        return reservation;
    }

    
    @Transactional
    public void cancelByReservationNumber(String reservationNumber) {
        Reservation reservation = reservationRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new IllegalArgumentException("예매 정보를 찾을 수 없습니다. 예매번호=" + reservationNumber));
        reservationRepository.delete(reservation);
    }

private String generateUniqueReservationNumber(int length) {
        // 안전장치: 중복될 가능성은 낮지만, UNIQUE 컬럼이라 충돌 시 재시도
        for (int i = 0; i < 50; i++) {
            String candidate = randomReservationCode(length);
            if (!reservationRepository.existsByReservationNumber(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("예매번호 생성에 실패했습니다. 다시 시도해 주세요.");
    }

    private String randomReservationCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = secureRandom.nextInt(RESERVATION_CHARS.length());
            sb.append(RESERVATION_CHARS.charAt(idx));
        }
        return sb.toString();
    }
    
    
    


    // ===== 마이페이지: 예매번호 찾기 =====
    @Transactional(readOnly = true)
    public List<Reservation> findByPhoneVariants(String phone) {
        if (phone == null || phone.isBlank()) return List.of();

        phone = phone.trim();

        Set<String> variants = new LinkedHashSet<>();
        variants.add(phone);

        String digits = digitsOnly(phone);
        if (!digits.isBlank()) {
            variants.add(digits);
            String hyphen = toHyphenPhone(digits);
            if (hyphen != null) variants.add(hyphen);
        }

        variants.removeIf(v -> v == null || v.isBlank());
        if (variants.isEmpty()) return List.of();

        return reservationRepository.findByPhoneInOrderByReservedAtDesc(new ArrayList<>(variants));
    }

    /** (B) member_id로 예매 조회 */
    @Transactional(readOnly = true)
    public List<Reservation> findByMemberId(Long memberId) {
        if (memberId == null) return List.of();
        return reservationRepository.findByMemberIdOrderByReservedAtDesc(memberId);
    }

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        String digits = phone.replaceAll("\\D", "");
        return digits.isBlank() ? null : digits;
    }

    private String digitsOnly(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    /**
     * 01012345678 -> 010-1234-5678 (11자리)
     */
    private String toHyphenPhone(String digits) {
        if (digits == null) return null;
        if (digits.length() == 11) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
        }
        return null;
    }
}
