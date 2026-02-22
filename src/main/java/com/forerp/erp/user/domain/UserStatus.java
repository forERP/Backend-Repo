package com.forerp.erp.user.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 계정 상태")
public enum UserStatus {
    @Schema(description = "활성") ACTIVE,
    @Schema(description = "비활성") INACTIVE
}