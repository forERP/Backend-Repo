package com.forerp.erp.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Schema(description = "묶음 상품 생성 요청")
public class ProductBundleCreateRequestDto {

    @NotBlank
    @Schema(description = "묶음 상품 이름", example = "아침 세트")
    private String name;

    @Schema(description = "묶음 상품 설명")
    private String description;

    @Schema(description = "이미지 URL")
    private String imageUrl;

    @NotNull
    @DecimalMin(value = "0.01")
    @Schema(description = "세트 정가 (최소 0.01)", example = "9000")
    private BigDecimal setPrice;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    @Schema(description = "할인율 (0~100%)", example = "10.0")
    private BigDecimal discountRate;

    @NotEmpty
    @Valid
    @Schema(description = "구성 상품 목록")
    private List<BundleItem> items;

    @Getter
    @Schema(description = "묶음 구성 항목")
    public static class BundleItem {

        @NotNull
        @Schema(description = "상품 ID", example = "1")
        private Long productId;

        @Positive
        @Schema(description = "수량 (최소 1)", example = "2")
        private Integer quantity;
    }
}