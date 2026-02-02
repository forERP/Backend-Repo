package com.forerp.erp.attendance.repository;

import com.forerp.erp.attendance.domain.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findTopByUser_IdAndClockOutIsNullOrderByClockInDesc(Long userId);
}