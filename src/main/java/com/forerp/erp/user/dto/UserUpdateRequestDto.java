package com.forerp.erp.user.dto;

import com.forerp.erp.user.domain.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequestDto {
    private String name;
    private String password;
    private UserRole role;
    private Long storeId;
}
