package com.forerp.erp.salary.service;

import com.forerp.erp.attendance.domain.Attendance;
import com.forerp.erp.attendance.repository.AttendanceRepository;
import com.forerp.erp.salary.domain.EmploymentType;
import com.forerp.erp.salary.domain.Salary;
import com.forerp.erp.salary.dto.SalaryDto;
import com.forerp.erp.salary.repository.SalaryRepository;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SalaryService {

    private final SalaryRepository salaryRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;

    // 일 8시간(480분) 초과 시 연장 근로, 수당 1.5배 적용
    private static final long STANDARD_WORK_MINUTES = 480;
    private static final BigDecimal OVERTIME_RATE = BigDecimal.valueOf(1.5);
    private static final BigDecimal MINUTES_IN_HOUR = BigDecimal.valueOf(60);
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    // 직원 급여 정보 등록 및 수정
    public void registerSalary(SalaryDto.Request request){
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (request.getEmploymentType() == null) {
            throw new IllegalArgumentException("고용 형태는 필수입니다.");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("급여 금액은 0보다 커야 합니다.");
        }
        if (request.getPaymentDate() == null) {
            throw new IllegalArgumentException("지급일은 필수입니다.");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        LocalDate byJoinDate = user.getCreatedAt().toLocalDate().plusMonths(1);
        LocalDate byNow = LocalDate.now().plusMonths(1);
        LocalDate minPaymentDate = byJoinDate.isAfter(byNow) ? byJoinDate : byNow;
        if (request.getPaymentDate().isBefore(minPaymentDate)) {
            throw new IllegalArgumentException("지급일은 직원 등록일 기준 한 달 이후(" + minPaymentDate + ")부터 지정할 수 있습니다.");
        }

        // // 시급직이면 시급만, 월급직이면 월급만 저장하도록 값 정리
        BigDecimal hourly = null;
        BigDecimal monthly = null;

        if(request.getEmploymentType() == EmploymentType.HOURLY){
            hourly = request.getAmount();
        }else{
            monthly = request.getAmount();
        }
        Salary salary = salaryRepository.findByUser_Id(request.getUserId()).orElse(null);

        if(salary != null) {
            salary.update(request.getEmploymentType(), hourly, monthly, request.getPaymentDate());
        }else{
            salaryRepository.save(new Salary(
                    user,
                    request.getEmploymentType(),
                    hourly,
                    monthly,
                    request.getPaymentDate()
            ));
        }
    }

    // 특정 연/월 급여 상세 내역 조회
    @Transactional(readOnly = true)
    public SalaryDto.Response calculatePayroll(Long userId, int year, int month) {
        Salary salary = salaryRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("급여 기준 정보가 없습니다."));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Attendance> attendances =
                attendanceRepository.findAllByUser_IdAndWorkDateBetweenOrderByWorkDateDesc(userId, startDate, endDate);

        long normalMinutes = 0;
        long overtimeMinutes = 0;

        // 일반 근무와 초과 근무(8시간 초과분)로 분리
        for (Attendance attendance : attendances) {
            if (attendance.getClockIn() != null && attendance.getClockOut() != null) {
                long dailyMinutes = Duration.between(attendance.getClockIn(), attendance.getClockOut()).toMinutes();

                if (dailyMinutes > STANDARD_WORK_MINUTES) {
                    normalMinutes += STANDARD_WORK_MINUTES;
                    overtimeMinutes += (dailyMinutes - STANDARD_WORK_MINUTES);
                } else {
                    normalMinutes += dailyMinutes;
                }
            }
        }

        // 분 단위를 시간 단위로 변환
        BigDecimal normalHours = BigDecimal.valueOf(normalMinutes).divide(MINUTES_IN_HOUR, 2, RoundingMode.HALF_UP);
        BigDecimal overtimeHours = BigDecimal.valueOf(overtimeMinutes).divide(MINUTES_IN_HOUR, 2, RoundingMode.HALF_UP);

        BigDecimal baseWage;
        BigDecimal normalPay;
        BigDecimal overtimePay;
        BigDecimal totalPay;

        // 고용 형태에 따른 급여 계산
        if (salary.getEmploymentType() == EmploymentType.HOURLY) {
            baseWage = salary.getHourlyWage();
            normalPay = normalHours.multiply(baseWage);
            overtimePay = overtimeHours.multiply(baseWage).multiply(OVERTIME_RATE);
            totalPay = normalPay.add(overtimePay);
        } else {
            baseWage = salary.getMonthlySalary();
            normalPay = baseWage;
            overtimePay = BigDecimal.ZERO;
            totalPay = baseWage;
        }

        // 최종 금액 원 단위(소수점 제거)
        normalPay = normalPay.setScale(0, RoundingMode.FLOOR);
        overtimePay = overtimePay.setScale(0, RoundingMode.FLOOR);
        totalPay = totalPay.setScale(0, RoundingMode.FLOOR);

        return new SalaryDto.Response(
                salary.getUser().getName(),
                salary.getEmploymentType(),
                baseWage,
                normalHours,
                overtimeHours,
                normalPay,
                overtimePay,
                totalPay,
                salary.getPaymentDate()
        );

    }
}
