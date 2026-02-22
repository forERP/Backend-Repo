package com.forerp.erp.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "카테고리에 속한 상품 요약")
public class ProductCategoryProductResponseDto {

    @Schema(description = "상품 ID", example = "1")
    private Long productId;

    @Schema(description = "SKU", example = "SKU-001")
    private String sku;

    @Schema(description = "상품 이름", example = "아메리카노")
    private String name;
}