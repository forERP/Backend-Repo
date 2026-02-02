package com.forerp.erp.salary.domain;

import com.forerp.erp.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


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

    @Column(name = "hourly_Wage")
    private Double hourlyWage;

    @Column(name = "monthly_salary")
    private Integer monthlySalary;

    @Builder
    public Salary(User user, EmploymentType employmentType, Double hourlyWage, Integer monthlySalary ){
        this.user = user;
        this.employmentType = employmentType;
        this.hourlyWage = hourlyWage;
        this.monthlySalary = monthlySalary;
    }




}
