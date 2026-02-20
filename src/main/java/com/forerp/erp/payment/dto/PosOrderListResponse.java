package com.forerp.erp.payment.dto;

import com.forerp.erp.payment.domain.ServiceMode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PosOrderListResponse {
    private List<PosOrderItem> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @Getter
    @AllArgsConstructor
    public static class PosOrderItem {
        private Long orderId;
        private Long paymentId;
        private String merchantOrderId;
        private String orderStatus;
        private String paymentStatus;
        private ServiceMode serviceMode;
        private BigDecimal totalAmount;
        private LocalDateTime orderedAt;
        private String outboundStatus;
        private String shipmentStatus;
    }
}

