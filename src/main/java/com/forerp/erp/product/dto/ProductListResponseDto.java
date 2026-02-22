package com.forerp.erp.product.dto;

import com.forerp.erp.product.domain.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Schema(description = "상품 목록 항목")
public class ProductListResponseDto {

    @Schema(description = "상품 ID", example = "1")
    private Long id;

    @Schema(description = "SKU", example = "SKU-001")
    private String sku;

    @Schema(description = "상품 이름", example = "아메리카노")
    private String name;

    @Schema(description = "카테고리 이름", example = "음료")
    private String categoryName;

    @Schema(description = "기준 판매가", example = "4000")
    private BigDecimal msrpPrice;

    @Schema(description = "상품 상태")
    private ProductStatus status;
}