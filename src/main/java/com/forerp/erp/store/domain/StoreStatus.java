package com.forerp.erp.store.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "매장 상태")
public enum StoreStatus {
    @Schema(description = "정상 영업")
    OPEN,

    @Schema(description = "임시 휴업")
    INACTIVE,

    @Schema(description = "영구 폐점 (데이터는 유지)")
    CLOSED
}