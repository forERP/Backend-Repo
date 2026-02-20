package com.forerp.erp.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseUpdateRequestDto {
    // allow partial updates: code, name, active
    private String code;
    private String name;
    private String address;
    private Boolean active;
}
