package com.forerp.erp.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PaymentListResponse {
    private List<PaymentSummaryResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}

