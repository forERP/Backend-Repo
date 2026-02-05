package com.forerp.erp.outbound.dto;

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
public class OutboundListResponse {

    private List<OutboundListItem> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutboundListItem {
        private Long outboundId;
        private Long orderId;
        private Long storeId;
        private Long warehouseId;
        private String status;         // CREATED/CONFIRMED/CANCELED
        private LocalDateTime createdAt;
        private String shipmentStatus; // READY/SHIPPING/ARRIVED
    }
}