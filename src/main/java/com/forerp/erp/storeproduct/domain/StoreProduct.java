package com.forerp.erp.storeproduct.domain;

import com.forerp.erp.inventory.domain.ChangeType;
import com.forerp.erp.inventory.domain.InventoryHistory;
import com.forerp.erp.inventory.domain.RefType;
import com.forerp.erp.product.domain.Product;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.warehouse.domain.Warehouse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "store_products",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"store_id", "warehouse_id", "product_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StoreProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

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

    public void changeSalePrice(BigDecimal salePrice) {
        if (salePrice == null) {
            throw new IllegalArgumentException("salePrice is required.");
        }
        if (salePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("salePrice must be greater than or equal to 0.");
        }
        this.salePrice = salePrice;
    }

    public void applyDefaultSalePriceIfUnset(BigDecimal defaultPrice) {
        if (defaultPrice == null) {
            return;
        }
        if (this.salePrice != null && this.salePrice.compareTo(BigDecimal.ZERO) > 0) {
            return;
        }
        if (defaultPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("defaultPrice must be greater than or equal to 0.");
        }
        this.salePrice = defaultPrice;
    }

    private boolean calculateSellable() {
        return saleStatus == SaleStatus.ON && quantity > 0;
    }

    public static StoreProduct create(Store store, Warehouse warehouse, Product product) {
        StoreProduct sp = new StoreProduct();
        sp.store = store;
        sp.warehouse = warehouse;
        sp.product = product;
        sp.quantity = 0;
        sp.salePrice = (product.getMsrpPrice() != null) ? product.getMsrpPrice() : BigDecimal.ZERO;
        sp.saleStatus = SaleStatus.ON;
        sp.stockThreshold = 0;
        sp.isSellable = false;
        return sp;
    }

    public InventoryHistory decreaseStock(
            int qty,
            RefType refType,
            Long refId,
            Long refItemId,
            User actor
    ) {
        if (qty <= 0) {
            throw new IllegalArgumentException("decrease qty must be greater than 0.");
        }
        if (this.quantity < qty) {
            throw new IllegalStateException("insufficient stock.");
        }

        int before = this.quantity;
        this.quantity -= qty;
        this.isSellable = calculateSellable();
        int after = this.quantity;

        return InventoryHistory.create(
                this,
                ChangeType.OUT,
                qty,
                before,
                after,
                refType,
                refId,
                refItemId,
                actor
        );
    }

    public InventoryHistory increaseStock(
            int qty,
            RefType refType,
            Long refId,
            Long refItemId,
            User actor
    ) {
        if (qty <= 0) {
            throw new IllegalArgumentException("increase qty must be greater than 0.");
        }

        int before = this.quantity;
        this.quantity += qty;
        this.isSellable = calculateSellable();
        int after = this.quantity;

        return InventoryHistory.create(
                this,
                ChangeType.IN,
                qty,
                before,
                after,
                refType,
                refId,
                refItemId,
                actor
        );
    }
}
