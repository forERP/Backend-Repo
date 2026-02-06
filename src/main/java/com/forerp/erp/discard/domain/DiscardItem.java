package com.forerp.erp.discard.domain;

import com.forerp.erp.inventory.domain.InventoryHistory;
import com.forerp.erp.inventory.domain.RefType;
import com.forerp.erp.storeproduct.domain.StoreProduct;
import com.forerp.erp.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "discard_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    /* ===== 생성 로직 ===== */
    public static DiscardItem create(StoreProduct storeProduct, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("폐기 수량은 0보다 커야 합니다.");
        }
        DiscardItem item = new DiscardItem();
        item.storeProduct = storeProduct;
        item.quantity = quantity;
        return item;
    }

    void assignDiscard(Discard discard) {
        this.discard = discard;
    }

    /* ===== 폐기 실행 → 재고 감소 + 히스토리 생성 ===== */
    public InventoryHistory discard(User actor) {
        if (this.discard == null || this.discard.getId() == null) {
            throw new IllegalStateException("폐기 문서에 연결되지 않은 항목입니다.");
        }

        return storeProduct.decreaseStock(
                quantity,
                RefType.DISCARD,
                discard.getId(),
                this.id,
                actor
        );
    }
}