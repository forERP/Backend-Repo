package com.forerp.erp.user.dto;

import com.forerp.erp.user.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDto {
    private String token;
    private UserRole role;
    private Long userId;
}