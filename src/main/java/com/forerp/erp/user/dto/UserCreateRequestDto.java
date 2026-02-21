package com.forerp.erp.user.dto;

import com.forerp.erp.user.domain.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserCreateRequestDto {

    @NotBlank
    @Size(max = 50)
    private String loginId;

    @NotBlank
    @Size(max = 50)
    private String employeeCode;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 20)
    private String phoneNumber;

    @NotNull
    private Long storeId;

    @NotNull
    private UserRole role;
}
