package com.forerp.erp.discard.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DiscardCreateRequest {

    @NotNull
    private Long storeId;

    @NotNull
    private Long warehouseId;

    private String reason;

    @Valid
    @NotNull
    private List<DiscardCreateItem> items;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class DiscardCreateItem {
        @NotNull
        private Long productId;

        @Min(1)
        private int qty;
    }
}