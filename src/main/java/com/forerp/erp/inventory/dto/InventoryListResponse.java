package com.forerp.erp.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryListResponse {
    private List<InventoryItem> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class InventoryItem {
        private Long productId;
        private String productName;
        private int onHand;
        private LocalDateTime updatedAt;
    }
}