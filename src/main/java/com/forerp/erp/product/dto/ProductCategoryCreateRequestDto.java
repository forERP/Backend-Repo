package com.forerp.erp.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "카테고리 생성 요청")
public class ProductCategoryCreateRequestDto {

    @NotBlank
    @Schema(description = "카테고리 코드 (영문 대문자 권장)", example = "BEVERAGE")
    private String code;

    @NotBlank
    @Schema(description = "카테고리 이름", example = "음료")
    private String name;

    @Schema(description = "카테고리 설명")
    private String description;

    @Schema(description = "카테고리 이미지 URL")
    private String imageUrl;

    @Schema(description = "활성 여부 (기본값: true)", example = "true")
    private Boolean active;
}