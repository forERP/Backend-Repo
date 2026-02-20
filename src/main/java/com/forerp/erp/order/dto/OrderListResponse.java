package com.forerp.erp.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderListResponse {

    private List<OrderListItem> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderListItem {
        private Long orderId;
        private Long storeId;
        private String storeName;
        private String storeCode;
        private Long warehouseId;
        private String warehouseCode;
        private String warehouseName;
        private String status;
        private BigDecimal totalAmount;
        private LocalDateTime orderedAt;
    }
}
