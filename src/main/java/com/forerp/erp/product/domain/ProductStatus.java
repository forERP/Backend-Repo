package com.forerp.erp.product.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 상태")
public enum ProductStatus {
    @Schema(description = "판매 활성") ACTIVE,
    @Schema(description = "단종") DISCONTINUED
}