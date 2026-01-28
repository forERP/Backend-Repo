package com.forerp.erp.product.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductCreateRequestDto {

    @NotBlank
    private String name;

    @NotNull
    private Long categoryId;

    private String description;

    private String imageUrl;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;
}