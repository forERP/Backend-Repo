package com.forerp.erp.inventory.dto;

import com.forerp.erp.inventory.domain.InventoryHistory;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class InventoryAdjustResponse {
    private Long inventoryHistoryId;
    private Long storeProductId;
    private Long storeId;
    private Long warehouseId;
    private Long productId;
    private InventoryAdjustRequest.AdjustmentType adjustmentType;
    private int adjustedQty;
    private int beforeQty;
    private int afterQty;
    private String memo;
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
