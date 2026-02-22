package com.forerp.erp.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "주문 생성 요청")
public class OrderCreateRequest {

    @NotNull
    @Schema(description = "주문 매장 ID", example = "1")
    private Long storeId;

    @NotNull
    @Schema(description = "입고 창고 ID", example = "1")
    private Long warehouseId;

    @Valid
    @NotNull
    @Schema(description = "주문 상품 목록")
    private List<OrderCreateItem> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "주문 상품 항목")
    public static class OrderCreateItem {

        @NotNull
        @Schema(description = "상품 ID", example = "1")
        private Long productId;

        @Min(1)
        @Schema(description = "수량 (최소 1)", example = "10")
        private int qty;
    }
}