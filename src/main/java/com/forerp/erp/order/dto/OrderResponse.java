package com.forerp.erp.order.dto;

import com.forerp.erp.order.domain.Order;
import com.forerp.erp.order.domain.OrderItem;
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
@Schema(description = "주문 상세 응답")
public class OrderResponse {

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

    @Schema(description = "총 주문 금액", example = "150000")
    private BigDecimal totalAmount;

    @Schema(description = "주문 일시")
    private LocalDateTime orderedAt;

    @Schema(description = "주문 상품 목록")
    private List<OrderItemResponse> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "주문 상품 항목 응답")
    public static class OrderItemResponse {

        @Schema(description = "주문 상품 ID", example = "1")
        private Long orderItemId;

        @Schema(description = "상품 ID", example = "1")
        private Long productId;

        @Schema(description = "상품 이름", example = "아메리카노")
        private String productName;

        @Schema(description = "수량", example = "10")
        private int qty;

        @Schema(description = "단가", example = "4500")
        private BigDecimal unitPrice;

        @Schema(description = "소계 (단가 × 수량)", example = "45000")
        private BigDecimal amount;

        public static OrderItemResponse from(OrderItem i) {
            return new OrderItemResponse(
                    i.getId(),
                    i.getProduct().getId(),
                    i.getProduct().getName(),
                    i.getQuantity(),
                    i.getUnitPrice(),
                    i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))
            );
        }
    }

    public static OrderResponse from(Order o) {
        return new OrderResponse(
                o.getId(),
                o.getStore().getId(),
                o.getStore().getName(),
                o.getStore().getStoreCode(),
                o.getWarehouse().getId(),
                o.getWarehouse().getCode(),
                o.getWarehouse().getName(),
                o.getStatus().name(),
                o.getTotalAmount(),
                o.getOrderedAt(),
                o.getItems().stream().map(OrderItemResponse::from).toList()
        );
    }
}