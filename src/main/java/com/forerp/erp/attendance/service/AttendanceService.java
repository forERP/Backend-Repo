package com.forerp.erp.attendance.service;

import com.forerp.erp.attendance.domain.Attendance;
import com.forerp.erp.attendance.domain.AttendanceStatus;
import com.forerp.erp.attendance.dto.AttendanceDto;
import com.forerp.erp.attendance.repository.AttendanceRepository;
import com.forerp.erp.attendance.service.support.AttendanceReader;
import com.forerp.erp.attendance.service.support.AttendanceResponseMapper;
import com.forerp.erp.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceReader attendanceReader;
    private final AttendanceResponseMapper mapper;

    // 출근 처리(POS)
    public AttendanceDto.Response clockIn(AttendanceDto.ClockInRequest request) {
        User user = attendanceReader.getUserForPos(request.getStoreCode(), request.getEmployeeCode());
        attendanceReader.validateNoDuplicateAttendance(user.getId(), LocalDate.now());

        Attendance attendance = Attendance.clockInBuilder()
                .user(user)
                .workDate(LocalDate.now())
                .clockIn(LocalDateTime.now())
                .build();

        Attendance saved = attendanceRepository.save(attendance);

        return mapper.toResponse(saved);
    }

    // 퇴근 처리(POS)
    public AttendanceDto.Response clockOut(AttendanceDto.ClockInRequest request) {
        User user = attendanceReader.getUserForPos(request.getStoreCode(),
                request.getEmployeeCode());

        Attendance attendance = attendanceRepository.findByUser_IdAndWorkDate(user.getId(), LocalDate.now()).orElseThrow(() -> new IllegalArgumentException("퇴근 처리할 출근 기록이 없습니다."));

        attendance.recordClockOut();

        return mapper.toResponse(attendance);
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
    public List<AttendanceDto.HistoryResponse> getStoreAttendanceHistory(Long storeId, LocalDate startDate, LocalDate endDate) {
        return attendanceReader.getHistory(storeId, startDate, endDate)
                .stream()
                .map(mapper::toHistoryResponse)
                .collect(Collectors.toList());
    }

    // 당일 출퇴근 상태 조회(POS)
    @Transactional(readOnly = true)
    public AttendanceDto.StatusResponse getTodayStatus(String storeCode, String employeeCode) {
        User user = attendanceReader.getUserForPos(storeCode, employeeCode);

        Optional<Attendance> today = attendanceRepository.findByUser_IdAndWorkDate(user.getId(), LocalDate.now());

        if (today.isEmpty()) {
            return new AttendanceDto.StatusResponse(user.getName(), null);
        }
        return new AttendanceDto.StatusResponse(user.getName(), today.get().getStatus());
    }
}



