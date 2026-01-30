package com.forerp.erp.salary.domain;

import com.forerp.erp.employee.domain.Employee;
import com.forerp.erp.employee.domain.EmploymentType;
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
    @Column(name = "salary_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false)
    private EmploymentType employmentType;

    @Column(name = "hourly_Wage")
    private Double hourlyWage;

    @Column(name = "monthly_salary")
    private Integer monthlySalary;

    @Builder
    public Salary(Employee employee, EmploymentType employmentType, Double hourlyWage, Integer monthlySalary ){
        this.employee = employee;
        this.employmentType = employmentType;
        this.hourlyWage = hourlyWage;
        this.monthlySalary = monthlySalary;
    }




}
