package com.forerp.erp.dashboard.dto;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        long todayOrderCount,
        BigDecimal todaySalesAmount,
        long todayOutboundCount,
        long todayInboundCount,
        long pendingPurchaseApprovalCount,
        long lowStockSkuCount,
        long delayedOrderCount,
        long pendingOutboundConfirmCount,
        long pendingInboundConfirmCount
) {
}
