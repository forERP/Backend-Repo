package com.forerp.erp.attendance.dto;

import com.forerp.erp.attendance.domain.AttendanceStatus;
import com.forerp.erp.user.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceDto {

    @Getter
    @NoArgsConstructor
    @Schema(description = "출퇴근 요청")
    public static class ClockInRequest {

        @Schema(description = "매장 코드", example = "STORE001")
        private String storeCode;

        @Schema(description = "사원번호", example = "EMP001")
        private String employeeCode;
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "현재 출퇴근 상태 응답")
    public static class StatusResponse {

        @Schema(description = "직원 이름", example = "홍길동")
        private String userName;

        @Schema(description = "역할")
        private UserRole role;

        @Schema(description = "근태 상태")
        private AttendanceStatus status;

        @Schema(description = "출근 시각")
        private LocalDateTime clockIn;

        @Schema(description = "퇴근 시각")
        private LocalDateTime clockOut;
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "출퇴근 처리 결과 응답")
    public static class Response {

        @Schema(description = "근태 기록 ID", example = "1")
        private Long attendanceId;

        @Schema(description = "직원 이름", example = "홍길동")
        private String userName;

        @Schema(description = "출근 시각")
        private LocalDateTime clockIn;

        @Schema(description = "퇴근 시각")
        private LocalDateTime clockOut;

        @Schema(description = "근태 상태")
        private AttendanceStatus status;
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "근태 이력 항목")
    public static class HistoryResponse {

        @Schema(description = "근태 기록 ID", example = "1")
        private Long attendanceId;

        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "직원 이름", example = "홍길동")
        private String employeeName;

        @Schema(description = "근무 날짜", example = "2025-01-15")
        private LocalDate workDate;

        @Schema(description = "출근 시각")
        private LocalDateTime clockIn;

        @Schema(description = "퇴근 시각")
        private LocalDateTime clockOut;

        @Schema(description = "근태 상태")
        private AttendanceStatus status;

        @Schema(description = "근무 시간(분). clockIn/clockOut 모두 있을 때만 계산.")
        public long getWorkDurationMinutes() {
            if (clockIn != null && clockOut != null) {
                return java.time.Duration.between(clockIn, clockOut).toMinutes();
            }
            return 0;
        }
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "휴가 등록 요청")
    public static class LeaveRequest {

        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "휴가 날짜", example = "2025-01-15")
        private LocalDate leaveDate;
    }
}