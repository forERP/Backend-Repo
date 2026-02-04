package com.forerp.erp.outbound.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutboundCreateRequest {
    @NotNull
    private Long orderId;
    @NotNull private Long storeId;
    @NotNull private Long warehouseId;
    @NotNull private List<OutboundCreateItem> items;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class OutboundCreateItem {
        @NotNull private Long orderItemId;
        @Positive
        private int qty;
    }
}