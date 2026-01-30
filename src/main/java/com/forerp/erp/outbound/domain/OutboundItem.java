package com.forerp.erp.outbound.domain;

import com.forerp.erp.inventory.domain.InventoryHistory;
import com.forerp.erp.inventory.domain.RefType;
import com.forerp.erp.order.domain.OrderItem;
import com.forerp.erp.product.domain.StoreProduct;
import com.forerp.erp.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "outbound_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboundItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbound_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "outbound_id", nullable = false)
    private Outbound outbound;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_product_id", nullable = false)
    private StoreProduct storeProduct;

    @Column(nullable = false)
    private int quantity;

    /* ===== 생성 로직 ===== */
    public static OutboundItem create(
            OrderItem orderItem,
            StoreProduct storeProduct,
            int quantity
    ) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("출고 수량은 0보다 커야 합니다.");
        }

        OutboundItem item = new OutboundItem();
        item.orderItem = orderItem;
        item.storeProduct = storeProduct;
        item.quantity = quantity;
        return item;
    }

    void assignOutbound(Outbound outbound) {
        this.outbound = outbound;
    }

    /* ===== 출고 실행 → 재고 감소 + 히스토리 생성 ===== */
    public InventoryHistory ship(User actor) {
        return storeProduct.decreaseStock(
                quantity,
                RefType.OUTBOUND,
                outbound.getId(),
                this.id,
                actor
        );
    }
}