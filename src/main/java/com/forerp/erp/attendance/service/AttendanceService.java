package com.forerp.erp.attendance.service;

import com.forerp.erp.attendance.domain.Attendance;
import com.forerp.erp.attendance.dto.AttendanceDto;
import com.forerp.erp.attendance.repository.AttendanceRepository;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    // 출근 처리(POS)
    public AttendanceDto.Response clockIn(AttendanceDto.ClockInRequest request) {
        User user = userRepository.findByStore_IdAndEmployeeCode(request.getStoreId(), request.getEmployeeCode())
                .orElseThrow(() -> new IllegalArgumentException("해당 매장의 직원을 찾을 수 없습니다."));

        LocalDate today = LocalDate.now();

        if (attendanceRepository.existsByUser_IdAndWorkDate(user.getId(), today)) {
            throw new IllegalArgumentException("이미 금일 출근 기록이 존재합니다.");
        }

        Attendance attendance = Attendance.clockInBuilder()
                .user(user)
                .workDate(today)
                .clockIn(LocalDateTime.now())
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        return toResponse(saved);
    }

    // 퇴근 처리(POS)
    public AttendanceDto.Response clockOut(AttendanceDto.ClockInRequest request) {
        User user = userRepository.findByStore_IdAndEmployeeCode(request.getStoreId(), request.getEmployeeCode())
                .orElseThrow(() -> new IllegalArgumentException("해당 매장의 직원을 찾을 수 없습니다."));

        Attendance attendance = attendanceRepository.findTopByUser_IdAndClockOutIsNullOrderByClockInDesc(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("현재 근무 중인 상태가 아닙니다."));

        attendance.recordClockOut();

        return toResponse(attendance);
    }

    private AttendanceDto.Response toResponse(Attendance attendance) {
        return new AttendanceDto.Response(
                attendance.getId(),
                attendance.getUser().getName(),
                attendance.getClockIn(),
                attendance.getClockOut(),
                attendance.getStatus()
        );
    }

    // 매장별, 기간별 근태 조회
    public List<AttendanceDto.HistoryResponse> getStoreAttendanceHistory(Long storeId, LocalDate startDate, LocalDate endDate){

        return attendanceRepository.findByUser_Store_IdAndWorkDateBetweenOrderByWorkDateDesc(storeId, startDate, endDate)
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

    // 휴가 등록
    public void registerLeave(AttendanceDto.LeaveRequest request){
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if(attendanceRepository.existsByUser_IdAndWorkDate(user.getId(), request.getLeaveDate())){
            throw new IllegalArgumentException("해당 날짜에 이미 근무 또는 휴가 기록이 존재합니다.");
        }

        Attendance leave = Attendance.leaveRecordBuilder()
                .user(user)
                .workDate(request.getLeaveDate())
                .build();

        attendanceRepository.save(leave);
    }
}

