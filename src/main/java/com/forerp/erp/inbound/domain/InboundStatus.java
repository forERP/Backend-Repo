package com.forerp.erp.inbound.domain;

public enum InboundStatus {
    CREATED,     // 입고 생성 (예정)
    SHIPPING,    // 배송 중
    CONFIRMED   // 입고 확정 (재고 증가 완료)
}