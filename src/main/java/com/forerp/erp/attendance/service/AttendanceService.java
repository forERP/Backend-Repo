package com.forerp.erp.attendance.service;

import com.forerp.erp.attendance.domain.Attendance;
import com.forerp.erp.attendance.dto.AttendanceDto;
import com.forerp.erp.attendance.repository.AttendanceRepository;
import com.forerp.erp.attendance.service.support.AttendanceReader;
import com.forerp.erp.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceReader attendanceReader;

    // 출근 처리(POS)
    public AttendanceDto.Response clockIn(AttendanceDto.ClockInRequest request) {
        User user = attendanceReader.getUserForPos(request.getStoreId(), request.getEmployeeCode());
        attendanceReader.validateNoDuplicateAttendance(user.getId(), LocalDate.now());

        Attendance attendance = Attendance.clockInBuilder()
                .user(user)
                .workDate(LocalDate.now())
                .clockIn(LocalDateTime.now())
                .build();

        return toResponse(attendanceRepository.save(attendance));
    }

    // 퇴근 처리(POS)
    public AttendanceDto.Response clockOut(AttendanceDto.ClockInRequest request) {
        User user = attendanceReader.getUserForPos(request.getStoreId(),
        request.getEmployeeCode());

        Attendance attendance = attendanceReader.getWorkingAttendance(user.getId());

        attendance.recordClockOut();

        return toResponse(attendance);
    }

    // 휴가 등록
    public void registerLeave(AttendanceDto.LeaveRequest request) {
        User user = attendanceReader.getUser(request.getUserId());
        attendanceReader.validateNoDuplicateAttendance(user.getId(), request.getLeaveDate());

        Attendance leave = Attendance.leaveRecordBuilder()
                .user(user)
                .workDate(request.getLeaveDate())
                .build();

        attendanceRepository.save(leave);
    }

    // 조회
    @Transactional(readOnly = true)
    public List<AttendanceDto.HistoryResponse> getStoreAttendanceHistory(Long storeId, LocalDate startDate, LocalDate endDate){
        return attendanceReader.getHistory(storeId, startDate, endDate)
                .stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    private AttendanceDto.HistoryResponse toHistoryResponse(Attendance attendance){
        return new AttendanceDto.HistoryResponse(
                attendance.getId(),
                attendance.getUser().getId(),
                attendance.getUser().getName(),
                attendance.getWorkDate(),
                attendance.getClockIn(),
                attendance.getClockOut(),
                attendance.getStatus()
        );
    }

    private AttendanceDto.Response toResponse(Attendance attendance){
        return new AttendanceDto.Response(
                attendance.getId(),
                attendance.getUser().getName(),
                attendance.getClockIn(),
                attendance.getClockOut(),
                attendance.getStatus()
        );
    }
}


