package com.forerp.erp.order.domain;

import com.forerp.erp.product.domain.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /* ===== 생성 로직 ===== */
    public static OrderItem create(Product product, int quantity, BigDecimal unitPrice) {
        OrderItem item = new OrderItem();
        item.product = product;
        item.quantity = quantity;
        item.unitPrice = unitPrice;
        return item;
    }

    void assignOrder(Order order) {
        this.order = order;
    }

    BigDecimal calculateAmount() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}