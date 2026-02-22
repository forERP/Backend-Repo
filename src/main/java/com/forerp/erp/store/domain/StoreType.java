package com.forerp.erp.store.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "매장 유형")
public enum StoreType {
    @Schema(description = "본사")
    HQ,

    @Schema(description = "일반 매장")
    STORE
}