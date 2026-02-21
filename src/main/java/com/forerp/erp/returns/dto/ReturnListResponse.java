package com.forerp.erp.returns.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ReturnListResponse {
    private List<ReturnListItem> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @Getter
    @AllArgsConstructor
    public static class ReturnListItem {
        private Long returnId;
        private Long orderId;
        private Long paymentId;
        private Long storeId;
        private String storeName;
        private String storeCode;
        private String status;
        private boolean discardStock;
        private BigDecimal refundedAmount;
        private String orderStatusBefore;
        private String orderStatusAfter;
        private String processedByName;
        private LocalDateTime processedAt;
        private String reason;
    }
}

