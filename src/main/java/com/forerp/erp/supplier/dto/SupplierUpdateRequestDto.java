package com.forerp.erp.supplier.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "공급업체 수정 요청 (변경할 필드만 전송)")
@Getter
@NoArgsConstructor
public class SupplierUpdateRequestDto {

    @Schema(description = "공급업체명", example = "한국식재 주식회사")
    private String name;

    @Schema(description = "담당자명", example = "김담당")
    private String contactName;

    @Schema(description = "담당자 전화번호", example = "010-1234-5678")
    private String contactPhone;

    @Schema(description = "담당자 이메일", example = "contact@supplier.com")
    private String contactEmail;

    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    private String address;

    @Schema(description = "위도", example = "37.5025")
    private Double latitude;

    @Schema(description = "경도", example = "127.0276")
    private Double longitude;

    @Schema(description = "활성 여부", example = "true")
    private Boolean active;
}