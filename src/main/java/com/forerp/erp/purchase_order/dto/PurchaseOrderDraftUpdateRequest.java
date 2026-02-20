package com.forerp.erp.purchase_order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PurchaseOrderDraftUpdateRequest {

    @NotNull
    private Long supplierId;

    @NotNull
    private Long warehouseId;

    private LocalDate deliveryDueDate;

    @Size(max = 100)
    private String receiverName;

    @Size(max = 30)
    private String receiverPhone;

    @Size(max = 255)
    private String shippingAddress;

    @Size(max = 100)
    private String paymentTerms;

    @Size(max = 100)
    private String memo;
}
