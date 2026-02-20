package com.forerp.erp.user.dto;

import com.forerp.erp.user.domain.UserRole;
import com.forerp.erp.user.domain.UserStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequestDto {
    private String name;
    private String phoneNumber;
    private String password;
    private UserRole role;
    private UserStatus status;
    private Long storeId;
}
