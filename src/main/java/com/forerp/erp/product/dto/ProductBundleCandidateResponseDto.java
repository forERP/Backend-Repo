package com.forerp.erp.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Schema(description = "묶음 상품 구성 후보 항목")
public class ProductBundleCandidateResponseDto {

    @Schema(description = "상품 ID", example = "1")
    private Long productId;

    @Schema(description = "SKU", example = "SKU-001")
    private String sku;

    @Schema(description = "상품 이름", example = "아메리카노")
    private String name;

    @Schema(description = "카테고리 코드", example = "BEVERAGE")
    private String categoryCode;

    @Schema(description = "카테고리 이름", example = "음료")
    private String categoryName;

    @Schema(description = "기준 판매가", example = "4000")
    private BigDecimal price;

    @Schema(description = "이미지 URL")
    private String imageUrl;
}