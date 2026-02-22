package com.forerp.erp.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "창고 생성 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseRequestDto {

    @Schema(description = "매장 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long storeId;

    @Schema(description = "창고 코드", example = "WH001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String code;

    @Schema(description = "창고명", example = "강남 물류창고", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String name;

    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    private String address;

    @Schema(description = "위도", example = "37.5025")
    private Double latitude;

    @Schema(description = "경도", example = "127.0276")
    private Double longitude;
}