package com.forerp.erp.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "묶음 상품 생성 응답")
public class ProductBundleCreateResponseDto {

    @Schema(description = "묶음 번들 ID", example = "1")
    private Long bundleId;

    @Schema(description = "생성된 상품 ID", example = "1")
    private Long productId;

    @Schema(description = "자동 생성된 SKU", example = "SKU-B-001")
    private String sku;
}