package com.forerp.erp.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ProductCategoryUpdateRequestDto {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String description;

    private String imageUrl;

    private Boolean active;
}
