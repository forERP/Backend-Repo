package com.forerp.erp.store.domain;

public enum StoreStatus {
    OPEN, // 정상운영
    INACTIVE, // 일시 중단
    CLOSED // 영구 폐점 (데이터는 남김)
}