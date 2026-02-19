package com.forerp.erp.inventory.dto;

import com.forerp.erp.storeproduct.domain.SaleStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class InventoryUpdateRequest {

    private SaleStatus saleStatus;
    private BigDecimal salePrice;
}
