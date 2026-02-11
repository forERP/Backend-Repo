package com.forerp.erp.product.dto;

import com.forerp.erp.product.domain.Product;
import com.forerp.erp.product.domain.ProductStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductDto {

    @Getter
    @NoArgsConstructor
    // 상품 수정
    public static class UpdateRequest{
        private String name;
        private Long categoryId;
        private BigDecimal msrpPrice;
        private String description;
        private String imageUrl;
    }
    @Getter
    // 상품 단건 상세
    public static class DetailResponse{
        private final Long id;
        private final String sku;
        private final String name;
        private final CategoryInfo category;
        private final BigDecimal msrpPrice;
        private final String description;
        private final String imageUrl;
        private final ProductStatus status;
        private final LocalDateTime createdAt;
        private final LocalDateTime updateAt;

        public DetailResponse(Product product){
            this.id = product.getId();
            this.sku = product.getSku();
            this.name = product.getName();
            this.category = new CategoryInfo(product.getCategory().getId(), product.getCategory().getName());
            this.msrpPrice = product.getMsrpPrice();
            this.description = product.getDescription();
            this.imageUrl = product.getImageUrl();
            this.status = product.getStatus();
            this.createdAt = product.getCreatedAt();
            this.updateAt = product.getUpdatedAt();
        }

    }

    @Getter
    public static class CategoryInfo {
        private final Long id;
        private final String name;

        public CategoryInfo(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
