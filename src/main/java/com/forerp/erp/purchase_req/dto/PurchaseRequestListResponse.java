package com.forerp.erp.purchase_req.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestListResponse {

    private List<Item> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long purchaseRequestId;
        private Long storeId;
        private String status;
        private LocalDateTime createdAt;
    }
}