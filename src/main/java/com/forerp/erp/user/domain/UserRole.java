package com.forerp.erp.user.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 역할")
public enum UserRole {
    @Schema(description = "본사 관리자") HQ_ADMIN,
    @Schema(description = "매장 관리자") STORE_ADMIN,
    @Schema(description = "매장 홀 직원") STORE_HALL_STAFF,
    @Schema(description = "매장 주방 직원") STORE_KITCHEN_STAFF
}