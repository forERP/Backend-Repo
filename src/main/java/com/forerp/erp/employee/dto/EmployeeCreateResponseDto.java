package com.forerp.erp.employee.dto;


import lombok.Getter;

@Getter
public class EmployeeCreateResponseDto{

    private final Long Id;

    public EmployeeCreateResponseDto(Long id){
        this.Id = id;
    }
}