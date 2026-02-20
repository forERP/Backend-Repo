package com.forerp.erp.payment.dto;

import com.forerp.erp.order.dto.OrderResponse;
import com.forerp.erp.outbound.dto.OutboundResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PosOrderDetailResponse {
    private OrderResponse order;
    private PaymentSummaryResponse payment;
    private OutboundResponse outbound;
}

