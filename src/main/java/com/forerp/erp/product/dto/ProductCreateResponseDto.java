package com.forerp.erp.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "상품 생성 응답")
public class ProductCreateResponseDto {

    @Schema(description = "생성된 상품 ID", example = "1")
    private Long id;

    @Schema(description = "자동 생성된 SKU", example = "SKU-20250101-001")
    private String sku;
}