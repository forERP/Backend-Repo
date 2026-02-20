package com.forerp.erp.payment.dto;

import com.forerp.erp.order.dto.OrderResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentConfirmResponse {
    private PaymentSummaryResponse payment;
    private OrderResponse order;
}

