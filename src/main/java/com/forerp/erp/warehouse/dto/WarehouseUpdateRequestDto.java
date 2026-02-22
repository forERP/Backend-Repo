package com.forerp.erp.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "창고 수정 요청 (변경할 필드만 전송)")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseUpdateRequestDto {

    @Schema(description = "창고 코드", example = "WH001")
    private String code;

    @Schema(description = "창고명", example = "강남 물류창고")
    private String name;

    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    private String address;

    @Schema(description = "위도", example = "37.5025")
    private Double latitude;

    @Schema(description = "경도", example = "127.0276")
    private Double longitude;

    @Schema(description = "활성 여부", example = "true")
    private Boolean active;
}