package com.forerp.erp.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequestDto {

    @NotBlank
    private String identifier; // loginId 또는 employeeCode 다 가능하게 해둔건데 어차피 포스기는 비밀번호를 안 쓰니 걍 따로 만들긴 해야 할 듯

    @NotBlank
    private String password;
}