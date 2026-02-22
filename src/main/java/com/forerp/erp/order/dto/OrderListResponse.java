package com.forerp.erp.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주문 목록 페이지 응답")
public class OrderListResponse {

    @Schema(description = "주문 목록")
    private List<OrderListItem> content;

    @Schema(description = "현재 페이지 번호 (0부터)", example = "0")
    private int page;

    @Schema(description = "페이지 크기", example = "20")
    private int size;

    @Schema(description = "전체 건수", example = "100")
    private long totalElements;

    @Schema(description = "전체 페이지 수", example = "5")
    private int totalPages;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "주문 목록 항목")
    public static class OrderListItem {

        @Schema(description = "주문 ID", example = "1")
        private Long orderId;

        @Schema(description = "매장 ID", example = "1")
        private Long storeId;

        @Schema(description = "매장 이름", example = "강남점")
        private String storeName;

        @Schema(description = "매장 코드", example = "STORE001")
        private String storeCode;

        @Schema(description = "창고 ID", example = "1")
        private Long warehouseId;

        @Schema(description = "창고 코드", example = "WH001")
        private String warehouseCode;

        @Schema(description = "창고 이름", example = "중앙 창고")
        private String warehouseName;

        @Schema(description = "주문 상태", example = "PLACED")
        private String status;

        @Schema(description = "총 금액", example = "150000")
        private BigDecimal totalAmount;

        @Schema(description = "주문 일시")
        private LocalDateTime orderedAt;
    }
}