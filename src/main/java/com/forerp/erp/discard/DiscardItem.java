package com.forerp.erp.discard;

import com.forerp.erp.inventory.domain.InventoryHistory;
import com.forerp.erp.inventory.domain.RefType;
import com.forerp.erp.product.domain.StoreProduct;
import com.forerp.erp.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "discard_items")
@Getter
public class DiscardItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discard_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "discard_id", nullable = false)
    private Discard discard;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_product_id", nullable = false)
    private StoreProduct storeProduct;

    @Column(nullable = false)
    private int quantity;

    protected DiscardItem() {
    }

    public static DiscardItem create(
            Discard discard,
            StoreProduct storeProduct,
            int quantity
    ) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("폐기 수량은 0보다 커야 합니다.");
        }

        DiscardItem item = new DiscardItem();
        item.discard = discard;
        item.storeProduct = storeProduct;
        item.quantity = quantity;
        return item;
    }

    /* ===== 폐기 실행 → 재고 감소 + 히스토리 생성 ===== */
    public InventoryHistory discard(User actor) {
        return storeProduct.decreaseStock(
                quantity,
                RefType.DISCARD,
                discard.getId(),
                this.id,
                actor
        );
    }
}