package com.forerp.erp.inbound.domain;

import com.forerp.erp.inventory.domain.InventoryHistory;
import com.forerp.erp.inventory.domain.RefType;
import com.forerp.erp.product.domain.Product;
import com.forerp.erp.storeproduct.domain.StoreProduct;
import com.forerp.erp.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inbound_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InboundItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inbound_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inbound_id", nullable = false)
    private Inbound inbound;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_product_id", nullable = false)
    private StoreProduct storeProduct;

    @Column(nullable = false)
    private int quantity;

    /* ===== 생성 로직 ===== */
    public static InboundItem create(
            Product product,
            StoreProduct storeProduct,
            int quantity
    ) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("입고 수량은 0보다 커야 합니다.");
        }

        InboundItem item = new InboundItem();
        item.product = product;
        item.storeProduct = storeProduct;
        item.quantity = quantity;
        return item;
    }

    void assignInbound(Inbound inbound) {
        this.inbound = inbound;
    }

    /* ===== 입고 실행 → 재고 증가 + 히스토리 생성 ===== */
    public InventoryHistory receive(User actor) {
        return storeProduct.increaseStock(
                quantity,
                RefType.INBOUND,
                inbound.getId(),
                this.id,
                actor
        );
    }
}