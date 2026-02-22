package com.forerp.erp.attendance.controller;

import com.forerp.erp.attendance.dto.AttendanceDto;
import com.forerp.erp.attendance.service.AttendanceService;
import com.forerp.erp.common.exception.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "근태", description = "직원 출퇴근 및 근태 관리 API")
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(summary = "출근 처리 (POS)", description = "storeCode + employeeCode 로 출근을 기록합니다. 인증 불필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "출근 처리 성공",
                    content = @Content(schema = @Schema(implementation = AttendanceDto.Response.class))),
            @ApiResponse(responseCode = "400", description = "이미 출근 기록 존재 또는 입력 오류",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceDto.Response> clockIn(@RequestBody AttendanceDto.ClockInRequest request) {
        return ResponseEntity.ok(attendanceService.clockIn(request));
    }

    @Operation(summary = "퇴근 처리 (POS)", description = "storeCode + employeeCode 로 퇴근을 기록합니다. 인증 불필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "퇴근 처리 성공",
                    content = @Content(schema = @Schema(implementation = AttendanceDto.Response.class))),
            @ApiResponse(responseCode = "400", description = "출근 기록 없음 또는 입력 오류",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceDto.Response> clockOut(@RequestBody AttendanceDto.ClockInRequest request) {
        return ResponseEntity.ok(attendanceService.clockOut(request));
    }

    @Operation(summary = "매장 근태 이력 조회", description = "특정 매장의 기간별 전체 근태 기록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/history")
    public ResponseEntity<List<AttendanceDto.HistoryResponse>> getAttendanceHistory(
            @Parameter(description = "매장 ID", example = "1") @RequestParam Long storeId,
            @Parameter(description = "조회 시작일 (yyyy-MM-dd)", example = "2025-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "조회 종료일 (yyyy-MM-dd)", example = "2025-01-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(attendanceService.getStoreAttendanceHistory(storeId, startDate, endDate));
    }

    @Operation(summary = "직원 현재 출퇴근 상태 조회 (POS)", description = "storeCode + employeeCode 로 오늘의 출퇴근 상태를 반환합니다. 인증 불필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AttendanceDto.StatusResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/status")
    public ResponseEntity<AttendanceDto.StatusResponse> getAttendanceStatus(
            @Parameter(description = "매장 코드", example = "STORE001") @RequestParam String storeCode,
            @Parameter(description = "사원번호", example = "EMP001") @RequestParam String employeeCode) {
        return ResponseEntity.ok(attendanceService.getTodayStatus(storeCode, employeeCode));
    }

    @Operation(summary = "휴가 등록", description = "특정 직원의 특정 날짜를 휴가로 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "휴가 등록 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/leave")
    public ResponseEntity<Void> registerLeave(@RequestBody AttendanceDto.LeaveRequest request) {
        attendanceService.registerLeave(request);
        return ResponseEntity.ok().build();
    }
}