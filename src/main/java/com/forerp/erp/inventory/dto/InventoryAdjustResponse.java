package com.forerp.erp.inventory.dto;

import com.forerp.erp.inventory.domain.InventoryHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "재고 조정 결과 응답")
public class InventoryAdjustResponse {

    @Schema(description = "재고 변동 이력 ID", example = "1")
    private Long inventoryHistoryId;

    @Schema(description = "매장 상품 ID", example = "1")
    private Long storeProductId;

    @Schema(description = "매장 ID", example = "1")
    private Long storeId;

    @Schema(description = "창고 ID", example = "1")
    private Long warehouseId;

    @Schema(description = "상품 ID", example = "1")
    private Long productId;

    @Schema(description = "조정 방향")
    private InventoryAdjustRequest.AdjustmentType adjustmentType;

    @Schema(description = "조정 수량", example = "10")
    private int adjustedQty;

    @Schema(description = "조정 전 수량", example = "100")
    private int beforeQty;

    @Schema(description = "조정 후 수량", example = "90")
    private int afterQty;

    @Schema(description = "메모", example = "파손으로 인한 수량 감소")
    private String memo;

    @Schema(description = "조정 일시")
    private LocalDateTime adjustedAt;

    public static InventoryAdjustResponse of(
            InventoryHistory history,
            Long storeId,
            Long warehouseId,
            Long productId,
            InventoryAdjustRequest.AdjustmentType adjustmentType
    ) {
        return new InventoryAdjustResponse(
                history.getId(),
                history.getStoreProduct().getId(),
                storeId,
                warehouseId,
                productId,
                adjustmentType,
                history.getChangeQty(),
                history.getBeforeQty(),
                history.getAfterQty(),
                history.getMemo(),
                history.getCreatedAt()
        );
    }
}