package com.forerp.erp.attendance.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "근태 상태")
public enum AttendanceStatus {
    @Schema(description = "근무 중") WORK,
    @Schema(description = "결근") ABSENT,
    @Schema(description = "휴가") LEAVE,
    @Schema(description = "퇴근 완료") OUT
}