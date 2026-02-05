package com.forerp.erp.purchase_req.dto;

import com.forerp.erp.purchase_req.domain.PurchaseRequest;
import com.forerp.erp.purchase_req.domain.PurchaseRequestItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestResponse {

    private Long purchaseRequestId;
    private Long storeId;
    private Long requestedByUserId;
    private String memo;
    private String status;
    private LocalDateTime createdAt;
    private List<Item> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long purchaseRequestItemId;
        private Long productId;
        private int qty;

        public static Item from(PurchaseRequestItem it) {
            return new Item(it.getId(), it.getProduct().getId(), it.getQuantity());
        }
    }

    public static PurchaseRequestResponse from(PurchaseRequest pr) {
        return new PurchaseRequestResponse(
                pr.getId(),
                pr.getStore().getId(),
                pr.getRequestedBy() == null ? null : pr.getRequestedBy().getId(),
                pr.getMemo(),
                pr.getStatus().name(),
                pr.getCreatedAt(),
                pr.getItems().stream().map(Item::from).toList()
        );
    }
}