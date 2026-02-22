package com.forerp.erp.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "카테고리 상세 응답")
public class ProductCategoryDetailResponseDto {

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

    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시")
    private LocalDateTime updatedAt;

    @Schema(description = "해당 카테고리에 속한 상품 목록")
    private List<ProductCategoryProductResponseDto> products;
}