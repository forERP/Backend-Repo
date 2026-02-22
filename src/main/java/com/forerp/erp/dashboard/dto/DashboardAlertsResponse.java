package com.forerp.erp.dashboard.dto;

public record DashboardAlertsResponse(
        long pendingPurchaseApprovalCount,
        long lowStockSkuCount,
        long delayedOrderCount,
        long pendingOutboundConfirmCount,
        long pendingInboundConfirmCount
) {
}
