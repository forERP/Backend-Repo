package com.forerp.erp.discard.dto;

import com.forerp.erp.discard.domain.Discard;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DiscardResponse {

    private Long discardId;
    private Long storeId;
    private String storeName;
    private String storeCode;
    private Long warehouseId;
    private String warehouseName;
    private String warehouseCode;
    private String reason;

    private Long createdByUserId;
    private String createdByName;
    private String status; // CREATED/CONFIRMED/CANCELED

    private LocalDateTime createdAt;
    private LocalDateTime discardedAt;

    private List<DiscardItemResponse> items;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class DiscardItemResponse {
        private Long discardItemId;
        private Long storeProductId;
        private Long productId;
        private String productName;
        private String productSku;
        private int qty;
    }

    public static DiscardResponse from(Discard d) {
        return new DiscardResponse(
                d.getId(),
                d.getStore().getId(),
                d.getStore().getName(),
                d.getStore().getStoreCode(),
                d.getWarehouse().getId(),
                d.getWarehouse().getName(),
                d.getWarehouse().getCode(),
                d.getReason(),
                d.getCreatedBy() == null ? null : d.getCreatedBy().getId(),
                d.getCreatedBy() == null ? null : d.getCreatedBy().getName(),
                d.getStatus().name(),
                d.getCreatedAt(),
                d.getDiscardedAt(),
                d.getItems().stream()
                        .map(i -> new DiscardItemResponse(
                                i.getId(),
                                i.getStoreProduct() == null ? null : i.getStoreProduct().getId(),
                                i.getStoreProduct().getProduct().getId(),
                                i.getStoreProduct().getProduct().getName(),
                                i.getStoreProduct().getProduct().getSku(),
                                i.getQuantity()
                        ))
                        .toList()
        );
    }
}
