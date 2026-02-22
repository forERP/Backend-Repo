package com.forerp.erp.order.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 상태")
public enum OrderStatus {
    @Schema(description = "주문 접수") PLACED,
    @Schema(description = "준비 완료") PREPARED,
    @Schema(description = "배송 중") SHIPPED,
    @Schema(description = "입고 완료") ARRIVED,
    @Schema(description = "취소됨") CANCELED
}