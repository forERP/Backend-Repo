package com.forerp.erp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "로그인 요청")
public class LoginRequestDto {

    @NotBlank
    @Schema(description = "로그인 ID 또는 사원번호", example = "admin01")
    private String identifier;

    @NotBlank
    @Schema(description = "비밀번호", example = "password123")
    private String password;
}