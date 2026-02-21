package com.forerp.erp.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InventoryAdjustRequest {

    public enum AdjustmentType {
        IN,
        OUT
    }

    @NotNull
    private Long storeId;

    @NotNull
    private Long warehouseId;

    @NotNull
    private Long productId;

    @NotNull
    private AdjustmentType adjustmentType;

    @Min(1)
    private int quantity;

    @Size(max = 300)
    private String memo;
}
