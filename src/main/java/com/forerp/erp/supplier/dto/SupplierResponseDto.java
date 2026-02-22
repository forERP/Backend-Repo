package com.forerp.erp.supplier.dto;

import com.forerp.erp.supplier.domain.Supplier;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "공급업체 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponseDto {

    @Schema(description = "공급업체 ID", example = "1")
    private Long supplierId;

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
    private boolean active;

    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;

    public static SupplierResponseDto from(Supplier supplier) {
        return new SupplierResponseDto(
                supplier.getId(),
                supplier.getName(),
                supplier.getContactName(),
                supplier.getContactPhone(),
                supplier.getContactEmail(),
                supplier.getAddress(),
                supplier.getLatitude(),
                supplier.getLongitude(),
                supplier.isActive(),
                supplier.getCreatedAt()
        );
    }
}