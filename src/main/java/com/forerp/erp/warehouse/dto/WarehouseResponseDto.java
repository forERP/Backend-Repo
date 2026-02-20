package com.forerp.erp.warehouse.dto;

import com.forerp.erp.warehouse.domain.Warehouse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseResponseDto {

    private Long warehouseId;
    private String code;
    private String name;
    private String address;
    private Long storeId;
    private String storeName;
    private boolean active;
    private LocalDateTime createdAt;

    public static WarehouseResponseDto from(Warehouse warehouse) {
        return new WarehouseResponseDto(
                warehouse.getId(),
                warehouse.getCode(),
                warehouse.getName(),
                warehouse.getAddress(),
                warehouse.getStore().getId(),
                warehouse.getStore().getName(),
                warehouse.isActive()
                , warehouse.getCreatedAt()
        );
    }
}
