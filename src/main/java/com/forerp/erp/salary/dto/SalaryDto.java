package com.forerp.erp.salary.dto;

import com.forerp.erp.salary.domain.EmploymentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SalaryDto {

    @Getter
    @NoArgsConstructor
    // 급여 정보 등록/수정
    public static class Request{
        private Long userId;
        private EmploymentType employmentType;
        private BigDecimal amount;
        private LocalDate paymentDate;
    }

    @Getter
    @AllArgsConstructor
    // 급여 조회
    public static class Response{
        private String userName;
        private EmploymentType type;
        private BigDecimal baseWage;

        private BigDecimal normalWorkHours;
        private BigDecimal overtimeHours;

        private BigDecimal normalPay;
        private BigDecimal overtimePay;
        private BigDecimal totalPay;
        private LocalDate paymentDate;
    }
}
