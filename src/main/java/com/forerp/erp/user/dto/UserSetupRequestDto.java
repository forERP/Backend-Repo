package com.forerp.erp.user.dto;

import lombok.Getter;

@Getter
public class UserSetupRequestDto {
    private String loginId;
    private String password;
    private Long storeId;
    private String name;
}
