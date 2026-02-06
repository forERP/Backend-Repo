package com.forerp.erp.purchase_req.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class PurchaseRequestCreateRequest {

    @NotNull
    private Long storeId;

    @Size(max = 100)
    private String memo;

    @Valid
    @NotNull
    private List<Item> items;

    @Getter @Setter
    public static class Item {
        @NotNull
        private Long productId;

        @NotNull
        private Integer qty;
    }
}