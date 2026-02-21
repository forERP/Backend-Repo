package com.forerp.erp.returns.dto;

import com.forerp.erp.returns.domain.SalesReturn;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReturnResponse {
    private Long returnId;
    private Long orderId;
    private Long paymentId;
    private Long storeId;
    private String storeName;
    private String storeCode;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private String status;
    private String reason;
    private boolean discardStock;
    private BigDecimal refundedAmount;
    private String orderStatusBefore;
    private String orderStatusAfter;
    private Long processedById;
    private String processedByName;
    private LocalDateTime processedAt;

    public static ReturnResponse from(SalesReturn salesReturn) {
        return new ReturnResponse(
                salesReturn.getId(),
                salesReturn.getOrder().getId(),
                salesReturn.getPayment().getId(),
                salesReturn.getStore().getId(),
                salesReturn.getStore().getName(),
                salesReturn.getStore().getStoreCode(),
                salesReturn.getWarehouse() == null ? null : salesReturn.getWarehouse().getId(),
                salesReturn.getWarehouse() == null ? null : salesReturn.getWarehouse().getCode(),
                salesReturn.getWarehouse() == null ? null : salesReturn.getWarehouse().getName(),
                salesReturn.getStatus().name(),
                salesReturn.getReason(),
                salesReturn.isDiscardStock(),
                salesReturn.getRefundedAmount(),
                salesReturn.getOrderStatusBefore(),
                salesReturn.getOrderStatusAfter(),
                salesReturn.getProcessedBy() == null ? null : salesReturn.getProcessedBy().getId(),
                salesReturn.getProcessedBy() == null ? null : salesReturn.getProcessedBy().getName(),
                salesReturn.getProcessedAt()
        );
    }
}

