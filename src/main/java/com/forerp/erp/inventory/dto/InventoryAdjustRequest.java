package com.forerp.erp.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "재고 수동 조정 요청")
public class InventoryAdjustRequest {

    @Schema(description = "조정 방향")
    public enum AdjustmentType {
        @Schema(description = "입고 (재고 증가)") IN,
        @Schema(description = "출고 (재고 감소)") OUT
    }

    @NotNull
    @Schema(description = "매장 ID", example = "1")
    private Long storeId;

    @NotNull
    @Schema(description = "창고 ID", example = "1")
    private Long warehouseId;

    @NotNull
    @Schema(description = "상품 ID", example = "1")
    private Long productId;

    @NotNull
    @Schema(description = "조정 방향 (IN / OUT)")
    private AdjustmentType adjustmentType;

    @Min(1)
    @Schema(description = "조정 수량 (최소 1)", example = "10")
    private int quantity;

    @Size(max = 300)
    @Schema(description = "메모 (최대 300자)", example = "파손으로 인한 수량 감소")
    private String memo;
}