package com.forerp.erp.attendance.dto;

import com.forerp.erp.attendance.domain.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceDto {

    @Getter
    @NoArgsConstructor
    public static class ClockInRequest {
        private String storeCode;
        private String employeeCode;
    }

    @Getter
    @AllArgsConstructor
    public static class StatusResponse{
            private String userName;
            private String status;
    }

    @Getter
    @AllArgsConstructor
    public static class Response {
        private Long attendanceId;
        private String userName;
        private LocalDateTime clockIn;
        private LocalDateTime clockOut;
        private AttendanceStatus status;
    }

    @Getter
    @AllArgsConstructor
    public static class HistoryResponse {
        private Long attendanceId;
        private Long userId;
        private String employeeName;
        private LocalDate workDate;
        private LocalDateTime clockIn;
        private LocalDateTime clockOut;
        private AttendanceStatus status;

        // 근무 시간(분 단위) 계산
        public long getWorkDurationMinutes() {
            if (clockIn != null && clockOut != null) {
                return java.time.Duration.between(clockIn, clockOut).toMinutes();
            }
            return 0;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class LeaveRequest {
        private Long userId;
        private LocalDate leaveDate;
    }

}

