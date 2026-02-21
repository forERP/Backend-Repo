package com.forerp.erp.supplier.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SupplierUpdateRequestDto {

    private String name;
    private String contactName;
    private String contactPhone;
    private String contactEmail;

    private String address;
    private Double latitude;
    private Double longitude;
    private Boolean active;
}
