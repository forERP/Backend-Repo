package com.forerp.erp.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DashboardRecentOrderItemResponse(
        Long id,
        String storeName,
        String status,
        BigDecimal totalAmount,
        LocalDateTime createdAt
) {
}
