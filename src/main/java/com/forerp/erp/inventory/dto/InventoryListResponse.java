package com.forerp.erp.inventory.dto;

import com.forerp.erp.storeproduct.domain.SaleStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryListResponse {
    private List<InventoryItem> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryItem {
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
        private BigDecimal productPrice;
        private LocalDateTime updatedAt;
    }
}
