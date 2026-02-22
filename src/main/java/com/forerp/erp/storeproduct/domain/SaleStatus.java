package com.forerp.erp.storeproduct.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "판매 상태")
public enum SaleStatus {
    @Schema(description = "판매 중") ON,
    @Schema(description = "판매 중지") OFF
}