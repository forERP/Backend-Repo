package com.forerp.erp.user.dto;


import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.domain.UserStatus;
import lombok.Getter;

@Getter
public class UserResponseDto {
    private final Long id;
    private final String loginId;
    private final UserStatus status;
    private final String name;
    private final Long storeId;

    public UserResponseDto(User user){
        this.id = user.getId();
        this.loginId = user.getLoginId();
        this.status = user.getStatus();
        this.name = user.getName();
        this.storeId = (user.getStore() != null) ? user.getStore().getId() : null;
    }
}
