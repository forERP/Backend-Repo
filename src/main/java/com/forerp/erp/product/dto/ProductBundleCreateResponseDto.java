package com.forerp.erp.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductBundleCreateResponseDto {
    private Long bundleId;
    private Long productId;
    private String sku;
}
