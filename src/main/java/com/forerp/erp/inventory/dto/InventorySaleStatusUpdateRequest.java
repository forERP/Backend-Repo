package com.forerp.erp.inventory.dto;

import com.forerp.erp.storeproduct.domain.SaleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "판매 상태 변경 요청")
public class InventorySaleStatusUpdateRequest {

    @NotNull
    @Schema(description = "변경할 판매 상태 (ON / OFF)")
    private SaleStatus saleStatus;
}