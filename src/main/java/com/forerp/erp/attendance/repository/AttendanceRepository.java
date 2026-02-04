package com.forerp.erp.attendance.repository;

import com.forerp.erp.attendance.domain.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // 퇴근 처리용
    Optional<Attendance> findTopByUser_IdAndClockOutIsNullOrderByClockInDesc(Long userId);

    // 중복 출근 방지
    boolean existsByUser_IdAndWorkDate(Long userId, LocalDate workDate);

    // 특정 매장의 특정 기간 근태 조회
    List<Attendance> findByUser_Store_IdAndWorkDateBetweenOrderByWorkDateDesc(
            Long storeId,
            LocalDate startDate,
            LocalDate endDate
    );

    // 특정 직원의 근태 조회
    List<Attendance> findAllByUser_IdAndWorkDateBetweenOrderByWorkDateDesc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );
}