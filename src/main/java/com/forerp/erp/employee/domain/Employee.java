package com.forerp.erp.employee.domain;

import com.forerp.erp.salary.domain.Salary;
import com.forerp.erp.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Table(name = "employees")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Employee{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long id;

    @Column(name = "employee_code", nullable = false, unique = true, length = 50)
    private String employeeCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User userId;

    // 소속 매장
    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Salary salary;

    @Builder
    public Employee(String employeeCode, User user, Long userId, Long storeId, String name){
        this.employeeCode = employeeCode;
        this.userId = user;
        this.storeId = storeId;
        this.name = name;
        this.status = EmployeeStatus.ACTIVE;
    }

    // 직원 급여 정보를 연결하기 위한 메소드
    public void assignSalary(Salary salary){
        this.salary = salary;
    }
}