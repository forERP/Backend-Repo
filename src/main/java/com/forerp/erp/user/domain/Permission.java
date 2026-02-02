package com.forerp.erp.user.domain;

public enum Permission {
    // 유저 권한 관리
    USER_CREATE("유저 생성 권한"),
    USER_READ("유저 조회 권한"),
    USER_UPDATE("유저 수정 권한"),
    USER_DELETE("유저 삭제 권한"),

    // 상품 권한 관리
    PRODUCT_MANAGE("상품 관리 권한");

    private final String description;

    Permission(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }
}
