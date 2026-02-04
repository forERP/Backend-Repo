package com.forerp.erp.attendance.domain;

import com.forerp.erp.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "clock_in", nullable = true)
    private LocalDateTime clockIn; // 출근 시각

    @Column(name = "clock_out")
    private LocalDateTime clockOut; // 퇴근 시각

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    // 출근 또는 결근 기록(자동)
    @Builder(builderMethodName = "clockInBuilder")
    public Attendance(LocalDateTime clockIn, User user, LocalDate workDate){
        this.user = user;
        this.workDate = workDate;
        this.clockIn = clockIn;
        this.status = (clockIn != null) ? AttendanceStatus.WORK : AttendanceStatus.ABSENT;
    }

    // 휴가 기록(관리자가 휴가를 등록 할 떄 사용)
    @Builder(builderMethodName = "leaveRecordBuilder")
    public Attendance(User user, LocalDate workDate){
        this.user = user;
        this.workDate = workDate;
        this.status = AttendanceStatus.LEAVE;
        this.clockIn = null; // 수동 기록시 출근 시간은 없음
        this.clockOut = null; // 수동 기록시 퇴근 시간은 없음
    }

    // 퇴근 시간 기록
    public void recordClockOut(){
        this.clockOut = LocalDateTime.now();
    }


}