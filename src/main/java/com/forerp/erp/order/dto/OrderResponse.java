package com.forerp.erp.order.dto;

import com.forerp.erp.order.domain.Order;
import com.forerp.erp.order.domain.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long orderId;
    private Long storeId;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime orderedAt;
    private List<OrderItemResponse> items;

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long orderItemId;
        private Long productId;
        private int qty;
        private BigDecimal unitPrice;
        private BigDecimal amount;

        public static OrderItemResponse from(OrderItem i) {
            return new OrderItemResponse(
                    i.getId(),
                    i.getProduct().getId(),
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
                o.getStatus().name(),
                o.getTotalAmount(),
                o.getOrderedAt(),
                o.getItems().stream().map(OrderItemResponse::from).toList()
        );
    }
}