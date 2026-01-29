package com.forerp.erp.employee.service;

import com.forerp.erp.employee.domain.Attendance;
import com.forerp.erp.employee.domain.Employee;
import com.forerp.erp.employee.dto.EmployeeCreateRequestDto;
import com.forerp.erp.employee.repository.AttendanceRepository;
import com.forerp.erp.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmployeeService{

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;

    @Transactional
    public Long createEmployee(EmployeeCreateRequestDto request) {
        Employee newEmployee = Employee.builder()
                .name(request.getName())
                .storeId(request.getStoreId())
                .employmentType(request.getEmploymentType())
                .hourlyWage(request.getHourlyWage())
                .build();

        Employee savedEmployee = employeeRepository.save(newEmployee);
        return savedEmployee.getId();

    }

    // 출근 기록
    @Transactional
    public void clockIn(Long employeeId){
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 직원입니다."));

            Attendance newAttendance = Attendance.builder()
                    .employee(employee)
                    .clockIn(LocalDateTime.now())
                    .build();

            attendanceRepository.save(newAttendance);
    }

    // 퇴근 기록
    @Transactional
    public void clockOut(Long employeeId){
        Attendance attendanceToUpdate = attendanceRepository
                .findTopByEmployeeIdAndClockOutIsNullOrderByClockInDesc(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("퇴근 처리할 출근 기록이 없습니다."));

        attendanceToUpdate.recordClockOut();
    }

    }
