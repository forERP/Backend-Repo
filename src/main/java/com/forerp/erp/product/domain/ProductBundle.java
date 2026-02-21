package com.forerp.erp.product.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "product_bundles",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "product_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductBundle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_bundle_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(name = "discount_rate", precision = 5, scale = 2)
    private BigDecimal discountRate;

    @OneToMany(mappedBy = "bundle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductBundleItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static ProductBundle create(Product product, BigDecimal discountRate) {
        if (product == null || product.getId() == null) {
            throw new IllegalArgumentException("세트 상품 정보가 유효하지 않습니다.");
        }

        ProductBundle bundle = new ProductBundle();
        bundle.product = product;
        bundle.discountRate = normalizeDiscountRate(discountRate);
        return bundle;
    }

    public void addItem(Product componentProduct, int quantityPerBundle) {
        if (componentProduct == null || componentProduct.getId() == null) {
            throw new IllegalArgumentException("구성 상품 정보가 유효하지 않습니다.");
        }
        if (Objects.equals(componentProduct.getId(), this.product.getId())) {
            throw new IllegalArgumentException("세트 상품은 자기 자신을 구성품으로 가질 수 없습니다.");
        }
        if (quantityPerBundle <= 0) {
            throw new IllegalArgumentException("구성 수량은 1 이상이어야 합니다.");
        }

        ProductBundleItem item = ProductBundleItem.create(this, componentProduct, quantityPerBundle);
        this.items.add(item);
    }

    private static BigDecimal normalizeDiscountRate(BigDecimal discountRate) {
        if (discountRate == null) {
            return null;
        }
        if (discountRate.compareTo(BigDecimal.ZERO) < 0 || discountRate.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("할인율은 0 이상 100 이하여야 합니다.");
        }
        return discountRate;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
