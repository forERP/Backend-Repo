package com.forerp.erp.employee.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class Employee{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false)
    private EmploymentType employmentType;

    @Column(name = "hourly_wage")
    private Double hourlyWage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status;

    @Builder
    public Employee(Long userId, Long storeId, String name, EmploymentType employmentType,
                    Double hourlyWage){
        this.userId = userId;
        this.storeId = storeId;
        this.name = name;
        this.employmentType = employmentType;
        this.hourlyWage = hourlyWage;
        this.status = EmployeeStatus.ACTIVE;
    }

}