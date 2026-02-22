package com.forerp.erp.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Schema(description = "상품 생성 요청")
public class ProductCreateRequestDto {

    @NotBlank
    @Schema(description = "상품 이름", example = "아메리카노")
    private String name;

    @NotNull
    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "상품 설명")
    private String description;

    @Schema(description = "상품 이미지 URL")
    private String imageUrl;

    @NotNull
    @DecimalMin("0.0")
    @Schema(description = "기준 판매가 (최소 0)", example = "4000")
    private BigDecimal price;
}