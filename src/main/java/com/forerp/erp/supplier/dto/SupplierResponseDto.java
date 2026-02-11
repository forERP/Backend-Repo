package com.forerp.erp.supplier.dto;

import com.forerp.erp.supplier.domain.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponseDto {

    private Long supplierId;
    private String name;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String address;
    private boolean active;

    public static SupplierResponseDto from(Supplier supplier) {
        return new SupplierResponseDto(
                supplier.getId(),
                supplier.getName(),
                supplier.getContactName(),
                supplier.getContactPhone(),
                supplier.getContactEmail(),
                supplier.getAddress(),
                supplier.isActive()
        );
    }
}
