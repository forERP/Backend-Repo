package com.forerp.erp.discard.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DiscardListResponse {

    private List<DiscardListItem> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class DiscardListItem {
        private Long discardId;
        private Long storeId;
        private String storeName;
        private String storeCode;
        private Long warehouseId;
        private String warehouseName;
        private String warehouseCode;
        private Long createdByUserId;
        private String createdByName;
        private String status;
        private String reason;
        private LocalDateTime createdAt;
        private LocalDateTime discardedAt;
    }
}
