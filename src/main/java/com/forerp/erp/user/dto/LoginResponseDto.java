package com.forerp.erp.user.dto;

import com.forerp.erp.user.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "로그인 응답")
public class LoginResponseDto {

    @Schema(description = "JWT 액세스 토큰")
    private String token;

    @Schema(description = "사용자 역할")
    private UserRole role;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;
}