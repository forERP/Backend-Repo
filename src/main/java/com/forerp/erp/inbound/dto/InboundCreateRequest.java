package com.forerp.erp.inbound.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InboundCreateRequest {
    @NotNull
    private Long purchaseOrderId;
}