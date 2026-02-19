package com.forerp.erp.inventory.dto;

import com.forerp.erp.storeproduct.domain.SaleStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InventorySaleStatusUpdateRequest {

    @NotNull
    private SaleStatus saleStatus;
}
