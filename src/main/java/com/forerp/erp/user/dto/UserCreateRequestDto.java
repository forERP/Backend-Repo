package com.forerp.erp.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserCreateRequestDto {
    private String loginId;
    private String password;
    private Long roleId;
    private Long storeId;
}
