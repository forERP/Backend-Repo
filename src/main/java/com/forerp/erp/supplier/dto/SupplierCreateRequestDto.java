package com.forerp.erp.supplier.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SupplierCreateRequestDto {

    @NotBlank
    private String name;

    private String contactName;
    private String contactPhone;
    private String contactEmail;

    private String address;
    private Boolean active;
}
