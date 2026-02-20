package com.forerp.erp.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ProductCategoryDetailResponseDto {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String imageUrl;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ProductCategoryProductResponseDto> products;
}
