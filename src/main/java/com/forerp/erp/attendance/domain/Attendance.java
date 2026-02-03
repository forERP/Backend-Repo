package com.forerp.erp.attendance.domain;

import com.forerp.erp.user.domain.User;
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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "clock_in", nullable = false)
    private LocalDateTime clockIn; // 출근 시각

    @Column(name = "clock_out")
    private LocalDateTime clockOut; // 퇴근 시각

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    // 출근
    @Builder
    public Attendance(LocalDateTime clockIn, User user){
        this.user = user;
        this.clockIn = clockIn;
        this.status = AttendanceStatus.WORK;
    }

    // 퇴근
    public void recordClockOut(){

        this.clockOut = LocalDateTime.now();
    }


}