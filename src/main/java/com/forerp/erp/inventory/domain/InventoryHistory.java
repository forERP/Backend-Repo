package com.forerp.erp.inventory.domain;

import com.forerp.erp.product.domain.StoreProduct;
import com.forerp.erp.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_history")
@Getter
public class InventoryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_product_id", nullable = false)
    private StoreProduct storeProduct;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 10)
    private ChangeType changeType;

    @Column(name = "change_qty", nullable = false)
    private int changeQty;

    @Column(name = "before_qty", nullable = false)
    private int beforeQty;

    @Column(name = "after_qty", nullable = false)
    private int afterQty;

    @Enumerated(EnumType.STRING)
    @Column(name = "ref_type", nullable = false, length = 20)
    private RefType refType;

    @Column(name = "ref_id", nullable = false)
    private Long refId;

    @Column(name = "ref_item_id")
    private Long refItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private User actorUser;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected InventoryHistory() {
    }

    private InventoryHistory(
            StoreProduct storeProduct,
            ChangeType changeType,
            int changeQty,
            int beforeQty,
            int afterQty,
            RefType refType,
            Long refId,
            Long refItemId,
            User actorUser
    ) {
        this.storeProduct = storeProduct;
        this.changeType = changeType;
        this.changeQty = changeQty;
        this.beforeQty = beforeQty;
        this.afterQty = afterQty;
        this.refType = refType;
        this.refId = refId;
        this.refItemId = refItemId;
        this.actorUser = actorUser;
    }

    /* ===== 생성 로직 (감소/증가 공통) ===== */
    public static InventoryHistory create(
            StoreProduct storeProduct,
            ChangeType changeType,
            int changeQty,
            int beforeQty,
            int afterQty,
            RefType refType,
            Long refId,
            Long refItemId,
            User actorUser
    ) {
        return new InventoryHistory(
                storeProduct,
                changeType,
                changeQty,
                beforeQty,
                afterQty,
                refType,
                refId,
                refItemId,
                actorUser
        );
    }
}