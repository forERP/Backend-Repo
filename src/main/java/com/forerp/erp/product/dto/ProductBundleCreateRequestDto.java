package com.forerp.erp.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class ProductBundleCreateRequestDto {

    @NotBlank
    private String name;

    private String description;

    private String imageUrl;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal setPrice;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    private BigDecimal discountRate;

    @NotEmpty
    @Valid
    private List<BundleItem> items;

    @Getter
    public static class BundleItem {
        @NotNull
        private Long productId;

        @Positive
        private Integer quantity;
    }
}
