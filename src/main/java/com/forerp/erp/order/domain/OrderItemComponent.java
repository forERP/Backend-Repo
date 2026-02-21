package com.forerp.erp.order.domain;

import com.forerp.erp.product.domain.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "order_item_components",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"order_item_id", "component_product_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_component_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "component_product_id", nullable = false)
    private Product componentProduct;

    @Column(name = "quantity_per_order_item", nullable = false)
    private int quantityPerOrderItem;

    public static OrderItemComponent create(OrderItem orderItem, Product componentProduct, int quantityPerOrderItem) {
        if (orderItem == null) {
            throw new IllegalArgumentException("주문 항목 정보가 유효하지 않습니다.");
        }
        if (componentProduct == null || componentProduct.getId() == null) {
            throw new IllegalArgumentException("구성 상품 정보가 유효하지 않습니다.");
        }
        if (quantityPerOrderItem <= 0) {
            throw new IllegalArgumentException("구성 수량은 1 이상이어야 합니다.");
        }

        OrderItemComponent component = new OrderItemComponent();
        component.orderItem = orderItem;
        component.componentProduct = componentProduct;
        component.quantityPerOrderItem = quantityPerOrderItem;
        return component;
    }
}
