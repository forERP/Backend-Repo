package com.forerp.erp.inbound.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public class InboundCreateRequest {
    @NotNull
    private Long purchaseOrderId;
    @NotNull private Long storeId;
    @NotNull private Long warehouseId;
    @NotNull private List<InboundCreateItem> items;

    @Getter @Setter @NoArgsConstructor
    @AllArgsConstructor
    public static class InboundCreateItem {
        @NotNull private Long productId;
        @Positive
        private int qty;
        @Positive private int unitCost;
    }
}