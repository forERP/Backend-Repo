package com.forerp.erp.purchase_order.domain;

import com.forerp.erp.product.domain.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "purchase_order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    /* ===== 생성 ===== */
    public static PurchaseOrderItem create(
            Product product,
            int quantity
    ) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("발주 수량은 0보다 커야 합니다.");
        }

        PurchaseOrderItem item = new PurchaseOrderItem();
        item.product = product;
        item.quantity = quantity;
        return item;
    }

    void assignPurchaseOrder(PurchaseOrder po) {
        this.purchaseOrder = po;
    }
}