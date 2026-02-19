package com.forerp.erp.inventory.dto;

import com.forerp.erp.storeproduct.domain.SaleStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private Long storeProductId;
    private Long storeId;
    private String storeName;
    private String storeCode;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private Long productId;
    private String sku;
    private String productName;
    private int onHand;
    private SaleStatus saleStatus;
    private BigDecimal salePrice;
    private LocalDateTime updatedAt;
}
