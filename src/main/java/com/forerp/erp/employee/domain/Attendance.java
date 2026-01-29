package com.forerp.erp.employee.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attendance{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)

    private Employee employee;

    @Column(name = "clock_in", nullable = false)
    private LocalDateTime clockIn; // 출근 시각

    @Column(name = "clock_out")
    private LocalDateTime clockOut; // 퇴근 시각

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    // 출근
    @Builder
    public Attendance(Employee employee, LocalDateTime clockIn){
        this.employee = employee;
        this.clockIn = clockIn;
        this.status = AttendanceStatus.WORK;
    }

    // 퇴근
    public void recordClockOut(){
        this.clockOut = LocalDateTime.now();
    }


}
