package com.forerp.erp.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "카테고리 목록 항목")
public class ProductCategoryResponseDto {

    @Schema(description = "카테고리 ID", example = "1")
    private Long id;

    @Schema(description = "카테고리 코드", example = "BEVERAGE")
    private String code;

    @Schema(description = "카테고리 이름", example = "음료")
    private String name;

    @Schema(description = "카테고리 설명")
    private String description;

    @Schema(description = "이미지 URL")
    private String imageUrl;

    @Schema(description = "활성 여부")
    private boolean active;
}