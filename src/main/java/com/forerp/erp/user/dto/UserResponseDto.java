package com.forerp.erp.user.dto;


import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.domain.UserRole;
import com.forerp.erp.user.domain.UserStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponseDto {

    private final Long id;
    private final String loginId;
    private final String employeeCode;
    private final String name;
    private final String phoneNumber;
    private final Long storeId;
    private final String storeCode;
    private final String storeName;
    private final UserRole role;
    private final UserStatus status;
    private final LocalDateTime createdAt;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.loginId = user.getLoginId();
        this.employeeCode = user.getEmployeeCode();
        this.name = user.getName();
        this.phoneNumber = user.getPhoneNumber();
        this.storeId = (user.getStore() == null) ? null : user.getStore().getId();
        this.storeCode = (user.getStore() == null) ? null : user.getStore().getStoreCode();
        this.storeName = (user.getStore() == null) ? null : user.getStore().getName();
        this.role = user.getRole();
        this.status = user.getStatus();
        this.createdAt = user.getCreatedAt();
    }
}
