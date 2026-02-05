package com.forerp.erp.attendance.service.support;

import com.forerp.erp.attendance.domain.Attendance;
import com.forerp.erp.attendance.repository.AttendanceRepository;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AttendanceReader {

    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;

    public User getUser(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    public User getUserForPos(Long storeId, String employeeCode){
        return userRepository.findByStore_IdAndEmployeeCode(storeId, employeeCode)
                .orElseThrow(() -> new IllegalArgumentException("해당 매장의 직원을 찾을 수 없습니다."));
    }

    public Attendance getWorkingAttendance(Long userId){
        return attendanceRepository.findTopByUser_IdAndClockOutIsNullOrderByClockInDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("현재 근무 중인 상태가 아닙니다."));
    }

    // 기간별 조회
    public List<Attendance> getHistory(Long storeId, LocalDate start, LocalDate end){
        return attendanceRepository.findByUser_Store_IdAndWorkDateBetweenOrderByWorkDateDesc(storeId, start, end);

    }

    public void validateNoDuplicateAttendance(Long userId, LocalDate date){
        if(attendanceRepository.existsByUser_IdAndWorkDate(userId, date)){
            throw new IllegalStateException("해당 날짜에 이미 근무 또는 휴가 기록이 존재합니다.");
        }
    }
}
