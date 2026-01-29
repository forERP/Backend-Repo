package com.forerp.erp.employee.dto;

import com.forerp.erp.employee.domain.EmploymentType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmployeeCreateRequestDto{

    private String name;
    private Long storeId;
    private EmploymentType employmentType;
    private Double hourlyWage;
}
