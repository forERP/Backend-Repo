package com.forerp.erp.inventory.dto;

import com.forerp.erp.storeproduct.domain.SaleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@Schema(description = "재고 정보 수정 요청 (null 필드 제외)")
public class InventoryUpdateRequest {

    @Schema(description = "판매 상태 (ON / OFF)")
    private SaleStatus saleStatus;

    @Schema(description = "매장 판매가", example = "4500")
    private BigDecimal salePrice;
}