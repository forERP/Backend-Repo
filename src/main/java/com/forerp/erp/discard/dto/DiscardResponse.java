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
    private Long warehouseId;
    private String reason;

    private Long createdByUserId;
    private String status; // CREATED/CONFIRMED/CANCELED

    private LocalDateTime createdAt;
    private LocalDateTime discardedAt;

    private List<DiscardItemResponse> items;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class DiscardItemResponse {
        private Long discardItemId;
        private Long productId;
        private int qty;
    }

    public static DiscardResponse from(Discard d) {
        return new DiscardResponse(
                d.getId(),
                d.getStore().getId(),
                d.getWarehouse().getId(),
                d.getReason(),
                d.getCreatedBy() == null ? null : d.getCreatedBy().getId(),
                d.getStatus().name(),
                d.getCreatedAt(),
                d.getDiscardedAt(),
                d.getItems().stream()
                        .map(i -> new DiscardItemResponse(
                                i.getId(),
                                i.getStoreProduct().getProduct().getId(),
                                i.getQuantity()
                        ))
                        .toList()
        );
    }
}