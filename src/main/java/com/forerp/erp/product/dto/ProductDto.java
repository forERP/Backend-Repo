package com.forerp.erp.product.dto;

import com.forerp.erp.product.domain.Product;
import com.forerp.erp.product.domain.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductDto {

    @Getter
    @NoArgsConstructor
    @Schema(description = "상품 수정 요청 (null 필드 제외)")
    public static class UpdateRequest {

        @Schema(description = "상품 이름", example = "아메리카노")
        private String name;

        @Schema(description = "카테고리 ID", example = "1")
        private Long categoryId;

        @Schema(description = "기준 판매가", example = "4000")
        private BigDecimal msrpPrice;

        @Schema(description = "상품 설명")
        private String description;

        @Schema(description = "이미지 URL")
        private String imageUrl;
    }

    @Getter
    @Schema(description = "상품 상세 응답")
    public static class DetailResponse {

        @Schema(description = "상품 ID", example = "1")
        private final Long id;

        @Schema(description = "SKU", example = "SKU-001")
        private final String sku;

        @Schema(description = "상품 이름", example = "아메리카노")
        private final String name;

        @Schema(description = "카테고리 정보")
        private final CategoryInfo category;

        @Schema(description = "기준 판매가", example = "4000")
        private final BigDecimal msrpPrice;

        @Schema(description = "상품 설명")
        private final String description;

        @Schema(description = "이미지 URL")
        private final String imageUrl;

        @Schema(description = "상품 상태")
        private final ProductStatus status;

        @Schema(description = "생성 일시")
        private final LocalDateTime createdAt;

        @Schema(description = "마지막 수정 일시")
        private final LocalDateTime updateAt;

        public DetailResponse(Product product) {
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
    @Schema(description = "카테고리 요약 정보")
    public static class CategoryInfo {

        @Schema(description = "카테고리 ID", example = "1")
        private final Long id;

        @Schema(description = "카테고리 이름", example = "음료")
        private final String name;

        public CategoryInfo(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}