package com.forerp.erp.warehouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseRequestDto {

    @NotNull
    private Long storeId;

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String address;
    private Double latitude;
    private Double longitude;
}
