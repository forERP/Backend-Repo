package com.forerp.erp.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {
    private String identifier;
    private String password;
}
