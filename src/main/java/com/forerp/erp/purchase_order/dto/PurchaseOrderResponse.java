package com.forerp.erp.purchase_order.dto;

import com.forerp.erp.purchase_order.domain.PurchaseOrder;
import com.forerp.erp.purchase_order.domain.PurchaseOrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponse {

    private Long purchaseOrderId;
    private Long purchaseRequestId;
    private Long supplierId;
    private Long storeId;
    private Long warehouseId;
    private String memo;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime orderedAt;
    private List<Item> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long purchaseOrderItemId;
        private Long productId;
        private int qty;

        public static Item from(PurchaseOrderItem it) {
            return new Item(it.getId(), it.getProduct().getId(), it.getQuantity());
        }
    }

    public static PurchaseOrderResponse from(PurchaseOrder po) {
        return new PurchaseOrderResponse(
                po.getId(),
                po.getPurchaseRequest() == null ? null : po.getPurchaseRequest().getId(),
                po.getSupplier().getId(),
                po.getStore().getId(),
                po.getWarehouse().getId(),
                po.getMemo(),
                po.getStatus().name(),
                po.getCreatedAt(),
                po.getOrderedAt(),
                po.getItems().stream().map(Item::from).toList()
        );
    }
}