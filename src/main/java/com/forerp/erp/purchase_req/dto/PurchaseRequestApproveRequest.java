package com.forerp.erp.purchase_req.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PurchaseRequestApproveRequest {

    @NotNull
    private Long supplierId;

    @NotNull
    private Long warehouseId;

    @Size(max = 100)
    private String memo;
}