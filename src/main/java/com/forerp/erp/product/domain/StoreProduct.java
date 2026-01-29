package com.forerp.erp.product.domain;

import com.forerp.erp.store.domain.Store;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "store_products",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_store_product",
                        columnNames = {"store_id", "product_id"}
                )
        }
)
public class StoreProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sale_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal salePrice = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_status", nullable = false, length = 10)
    private SaleStatus saleStatus = SaleStatus.ON;

    @Column(nullable = false)
    private int quantity = 0;

    @Column(name = "stock_threshold", nullable = false)
    private int stockThreshold = 0;

    @Column(name = "is_sellable", nullable = false)
    private boolean isSellable = true;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStock(int newQuantity) {
        this.quantity = newQuantity;
        this.isSellable = calculateSellable();
    }

    public void changeSaleStatus(SaleStatus status) {
        this.saleStatus = status;
        this.isSellable = calculateSellable();
    }

    private boolean calculateSellable() {
        return saleStatus == SaleStatus.ON && quantity > 0;
    }

    /* ===== 재고 감소 ===== */
    public void decreaseStock(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("차감 수량은 0보다 커야 합니다.");
        }
        if (this.quantity < qty) {
            throw new IllegalStateException("재고 부족");
        }
        this.quantity -= qty;
        this.updatedAt = LocalDateTime.now();
    }

    /* ===== 재고 증가 (반품/입고) ===== */
    public void increaseStock(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("증가 수량은 0보다 커야 합니다.");
        }
        this.quantity += qty;
        this.updatedAt = LocalDateTime.now();
    }
}