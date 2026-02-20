package com.forerp.erp.payment.dto;

import com.forerp.erp.payment.domain.ServiceMode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PaymentPrepareResponse {
    private Long paymentId;
    private String merchantOrderId;
    private String customerKey;
    private String orderName;
    private BigDecimal amount;
    private String currency;
    private ServiceMode serviceMode;
    private String clientKey;
}

