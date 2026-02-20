package com.forerp.erp.payment.service;

import lombok.Getter;

@Getter
public class TossPaymentsException extends RuntimeException {

    private final int httpStatus;
    private final String responseBody;

    public TossPaymentsException(String message, int httpStatus, String responseBody) {
        super(message);
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
    }
}

