package com.forerp.erp.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductCategoryProductResponseDto {

    private Long productId;
    private String sku;
    private String name;
}
