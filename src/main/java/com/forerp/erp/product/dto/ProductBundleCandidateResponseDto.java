package com.forerp.erp.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ProductBundleCandidateResponseDto {
    private Long productId;
    private String sku;
    private String name;
    private String categoryCode;
    private String categoryName;
    private BigDecimal price;
    private String imageUrl;
}
