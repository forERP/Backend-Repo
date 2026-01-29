package com.forerp.erp.employee.repository;

import com.forerp.erp.employee.domain.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findTopByEmployeeIdAndClockOutIsNullOrderByClockInDesc(Long employeeId);
}