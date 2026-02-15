package com.forerp.erp.purchase_order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderListResponse {

    private List<Item> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long purchaseOrderId;
        private Long purchaseRequestId;
        private Long supplierId;
        private String supplierName;
        private Long storeId;
        private String storeName;
        private String storeCode;
        private Long warehouseId;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime orderedAt;
    }
}
