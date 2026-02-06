package com.forerp.erp.product.dto;

import com.forerp.erp.product.domain.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ProductListResponseDto {
    private Long id;
    private String sku;
    private String name;
    private String categoryName;
    private BigDecimal msrpPrice;
    private ProductStatus status;
}
