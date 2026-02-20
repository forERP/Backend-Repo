package com.forerp.erp.payment.dto;

import com.forerp.erp.payment.domain.Payment;
import com.forerp.erp.payment.domain.ServiceMode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PaymentSummaryResponse {
    private Long paymentId;
    private String merchantOrderId;
    private Long orderId;
    private String orderStatus;
    private BigDecimal orderTotalAmount;
    private LocalDateTime orderedAt;
    private Long storeId;
    private String storeName;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private String paymentStatus;
    private ServiceMode serviceMode;
    private BigDecimal amount;
    private BigDecimal approvedAmount;
    private BigDecimal canceledAmount;
    private String method;
    private LocalDateTime preparedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime canceledAt;

    public static PaymentSummaryResponse from(Payment payment) {
        return new PaymentSummaryResponse(
                payment.getId(),
                payment.getMerchantOrderId(),
                payment.getOrder() == null ? null : payment.getOrder().getId(),
                payment.getOrder() == null ? null : payment.getOrder().getStatus().name(),
                payment.getOrder() == null ? null : payment.getOrder().getTotalAmount(),
                payment.getOrder() == null ? null : payment.getOrder().getOrderedAt(),
                payment.getStore().getId(),
                payment.getStore().getName(),
                payment.getWarehouse() == null ? null : payment.getWarehouse().getId(),
                payment.getWarehouse() == null ? null : payment.getWarehouse().getCode(),
                payment.getWarehouse() == null ? null : payment.getWarehouse().getName(),
                payment.getStatus().name(),
                payment.getServiceMode(),
                payment.getAmount(),
                payment.getApprovedAmount(),
                payment.getCanceledAmount(),
                payment.getMethod(),
                payment.getPreparedAt(),
                payment.getApprovedAt(),
                payment.getCanceledAt()
        );
    }
}
