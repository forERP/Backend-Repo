package com.forerp.erp.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "product_bundle_items",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"product_bundle_id", "component_product_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductBundleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_bundle_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_bundle_id", nullable = false)
    private ProductBundle bundle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "component_product_id", nullable = false)
    private Product componentProduct;

    @Column(name = "quantity_per_bundle", nullable = false)
    private int quantityPerBundle;

    public static ProductBundleItem create(ProductBundle bundle, Product componentProduct, int quantityPerBundle) {
        if (bundle == null || bundle.getId() == null && bundle.getProduct() == null) {
            throw new IllegalArgumentException("세트 정보가 유효하지 않습니다.");
        }
        if (componentProduct == null || componentProduct.getId() == null) {
            throw new IllegalArgumentException("구성 상품 정보가 유효하지 않습니다.");
        }
        if (quantityPerBundle <= 0) {
            throw new IllegalArgumentException("구성 수량은 1 이상이어야 합니다.");
        }

        ProductBundleItem item = new ProductBundleItem();
        item.bundle = bundle;
        item.componentProduct = componentProduct;
        item.quantityPerBundle = quantityPerBundle;
        return item;
    }
}
