package com.forerp.erp.attendance.controller;

import com.forerp.erp.attendance.dto.AttendanceDto;
import com.forerp.erp.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // POS 출근
    @PostMapping("/clock_in")
    public ResponseEntity<AttendanceDto.Response> clockIn(@RequestBody AttendanceDto.ClockInRequest request) {
        return ResponseEntity.ok(attendanceService.clockIn(request));
    }

    // POS 퇴근
    @PostMapping("/clock_out")
    public ResponseEntity<AttendanceDto.Response> clockOut(@RequestBody AttendanceDto.ClockInRequest request) {
        return ResponseEntity.ok(attendanceService.clockOut(request));
    }

    // 관리자용 근태 기록 조회
    @GetMapping("/history")
    public ResponseEntity<List<AttendanceDto.HistoryResponse>> getAttendanceHistory(
            Long storeId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate startDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate endDate){

        return ResponseEntity.ok(attendanceService.getStoreAttendanceHistory(storeId, startDate, endDate));
    }

    // 휴가 등록
    public ResponseEntity<Void> registerLeave(@RequestBody AttendanceDto.LeaveRequest request){
        attendanceService.registerLeave(request);
        return ResponseEntity.ok().build();
    }
}
