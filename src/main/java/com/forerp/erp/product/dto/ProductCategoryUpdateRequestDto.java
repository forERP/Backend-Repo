package com.forerp.erp.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "카테고리 수정 요청")
public class ProductCategoryUpdateRequestDto {

    @NotBlank
    @Schema(description = "카테고리 코드", example = "BEVERAGE")
    private String code;

    @NotBlank
    @Schema(description = "카테고리 이름", example = "음료")
    private String name;

    @Schema(description = "카테고리 설명")
    private String description;

    @Schema(description = "이미지 URL")
    private String imageUrl;

    @Schema(description = "활성 여부", example = "true")
    private Boolean active;
}