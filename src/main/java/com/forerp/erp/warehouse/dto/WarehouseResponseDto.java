package com.forerp.erp.warehouse.dto;

import com.forerp.erp.warehouse.domain.Warehouse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Schema(description = "창고 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseResponseDto {

    @Schema(description = "창고 ID", example = "1")
    private Long warehouseId;

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

    @Schema(description = "소속 매장 ID", example = "1")
    private Long storeId;

    @Schema(description = "소속 매장명", example = "강남점")
    private String storeName;

    @Schema(description = "활성 여부", example = "true")
    private boolean active;

    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;

    public static WarehouseResponseDto from(Warehouse warehouse) {
        return new WarehouseResponseDto(
                warehouse.getId(),
                warehouse.getCode(),
                warehouse.getName(),
                warehouse.getAddress(),
                warehouse.getLatitude(),
                warehouse.getLongitude(),
                warehouse.getStore().getId(),
                warehouse.getStore().getName(),
                warehouse.isActive(),
                warehouse.getCreatedAt()
        );
    }
}