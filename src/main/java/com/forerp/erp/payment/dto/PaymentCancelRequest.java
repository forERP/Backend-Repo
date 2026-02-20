package com.forerp.erp.payment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PaymentCancelRequest {

    @NotBlank
    private String reason;

    @NotNull
    private Boolean discardStock = Boolean.FALSE;

    @Valid
    private List<PaymentCancelItem> items;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PaymentCancelItem {
        @NotNull
        private Long orderItemId;

        @Min(1)
        private int qty;
    }
}

