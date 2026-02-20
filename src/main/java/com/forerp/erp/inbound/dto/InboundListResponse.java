package com.forerp.erp.inbound.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InboundListResponse {

    private List<InboundListItem> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InboundListItem {
        private Long inboundId;
        private Long purchaseOrderId;
        private LocalDateTime purchaseOrderCreatedAt;
        private Long storeId;
        private String storeName;
        private String storeCode;
        private Long warehouseId;
        private String status;
        private LocalDateTime createdAt;
        private String shipmentStatus;
    }
}
