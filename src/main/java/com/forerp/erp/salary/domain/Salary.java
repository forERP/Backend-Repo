package com.forerp.erp.salary.domain;

import com.forerp.erp.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "salaries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false)
    private EmploymentType employmentType;

    @Column(name = "hourly_wage")
    private BigDecimal hourlyWage;

    @Column(name = "monthly_salary")
    private BigDecimal monthlySalary;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Builder
    public Salary(
            User user,
            EmploymentType employmentType,
            BigDecimal hourlyWage,
            BigDecimal monthlySalary,
            LocalDate paymentDate
    ) {
        this.user = user;
        this.employmentType = employmentType;
        this.hourlyWage = hourlyWage;
        this.monthlySalary = monthlySalary;
        this.paymentDate = paymentDate;
    }

    public void update(
            EmploymentType employmentType,
            BigDecimal hourlyWage,
            BigDecimal monthlySalary,
            LocalDate paymentDate
    ) {
        this.employmentType = employmentType;
        this.monthlySalary = monthlySalary;
        this.hourlyWage = hourlyWage;
        this.paymentDate = paymentDate;
    }
}
