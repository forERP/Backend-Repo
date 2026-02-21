package com.forerp.erp.order.domain;

import com.forerp.erp.product.domain.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_items")
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

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemComponent> components = new ArrayList<>();

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

    public void addComponent(Product componentProduct, int quantityPerOrderItem) {
        OrderItemComponent component = OrderItemComponent.create(this, componentProduct, quantityPerOrderItem);
        this.components.add(component);
    }

    BigDecimal calculateAmount() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal reduceQuantity(int qtyToCancel) {
        if (qtyToCancel <= 0) {
            throw new IllegalArgumentException("취소 수량은 1 이상이어야 합니다.");
        }
        if (qtyToCancel > this.quantity) {
            throw new IllegalArgumentException("취소 수량이 주문 수량을 초과했습니다.");
        }

        BigDecimal canceledAmount = this.unitPrice.multiply(BigDecimal.valueOf(qtyToCancel));
        this.quantity -= qtyToCancel;
        return canceledAmount;
    }
}
