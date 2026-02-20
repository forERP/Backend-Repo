package com.forerp.erp.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductCategoryResponseDto {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String imageUrl;
    private boolean active;
}
