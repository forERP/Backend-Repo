package com.forerp.erp.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class OrderCreateRequest {

    @NotNull
    private Long storeId;

    @NotNull
    private Long warehouseId;

    @Valid
    @NotNull
    private List<OrderCreateItem> items;

    @Getter @Setter
    @NoArgsConstructor
    public static class OrderCreateItem {

        @NotNull
        private Long productId;

        @Min(1)
        private int qty;
    }
}