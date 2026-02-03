package com.forerp.erp.purchase_req.domain;

import com.forerp.erp.product.domain.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "purchase_request_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_request_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_request_id", nullable = false)
    private PurchaseRequest purchaseRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    /* ===== 생성 ===== */
    public static PurchaseRequestItem create(
            Product product,
            int quantity
    ) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("요청 수량은 0보다 커야 합니다.");
        }

        PurchaseRequestItem item = new PurchaseRequestItem();
        item.product = product;
        item.quantity = quantity;
        return item;
    }

    void assignPurchaseRequest(PurchaseRequest pr) {
        this.purchaseRequest = pr;
    }
}