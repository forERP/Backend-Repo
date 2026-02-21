package com.forerp.erp.inventory.dto;

import com.forerp.erp.inventory.domain.ChangeType;
import com.forerp.erp.inventory.domain.RefType;

import java.time.LocalDateTime;

public record InventoryLogListItemResponse(
        Long logId,
        RefType eventType,
        ChangeType changeType,
        int changeQty,
        int beforeQty,
        int afterQty,
        Long refId,
        Long refItemId,
        LocalDateTime movedAt,
        Long storeProductId,
        Long storeId,
        String storeName,
        String storeCode,
        Long warehouseId,
        String warehouseName,
        String warehouseCode,
        Long productId,
        String productSku,
        String productName,
        Long actorUserId,
        String actorName,
        String actorEmployeeCode,
        String memo
) {
}
