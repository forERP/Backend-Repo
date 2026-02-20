package com.forerp.erp.payment.dto;

import com.forerp.erp.payment.domain.ServiceMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PaymentPrepareRequest {

    @NotNull
    private ServiceMode serviceMode = ServiceMode.DINE_IN;

    @NotNull
    @NotEmpty
    @Valid
    private List<PaymentPrepareItem> items;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PaymentPrepareItem {
        @NotNull
        private Long productId;

        @Min(1)
        private int qty;
    }
}

