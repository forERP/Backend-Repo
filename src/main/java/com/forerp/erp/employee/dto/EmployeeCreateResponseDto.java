package com.forerp.erp.employee.dto;


import lombok.Getter;

@Getter
public class EmployeeCreateResponseDto{

    private final Long Id;
    private final String employeeCode;

    public EmployeeCreateResponseDto(Long id, String employeeCode){
        this.Id = id;
        this.employeeCode = employeeCode;
    }
}