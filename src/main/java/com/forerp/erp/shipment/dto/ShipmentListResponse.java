package com.forerp.erp.shipment.dto;

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
public class ShipmentListResponse {

    private List<ShipmentListItem> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShipmentListItem {
        private Long shipmentId;
        private String flowType;
        private Long inboundId;
        private Long outboundId;
        private Long referenceId;
        private Long storeId;
        private String storeName;
        private String storeCode;
        private Long warehouseId;
        private String warehouseName;
        private String warehouseCode;
        private String status;
        private String carrierCode;
        private String carrier;
        private String trackingNumber;
        private LocalDateTime createdAt;
        private LocalDateTime departedAt;
        private LocalDateTime arrivedAt;
    }
}
