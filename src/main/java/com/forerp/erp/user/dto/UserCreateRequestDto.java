package com.forerp.erp.user.dto;

import com.forerp.erp.user.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "사용자 생성 요청")
public class UserCreateRequestDto {

    @NotBlank
    @Size(max = 50)
    @Schema(description = "로그인 ID (최대 50자)", example = "user01")
    private String loginId;

    @NotBlank
    @Size(max = 50)
    @Schema(description = "사원번호 (최대 50자)", example = "EMP001")
    private String employeeCode;

    @NotBlank
    @Size(min = 8, max = 100)
    @Schema(description = "비밀번호 (8~100자)", example = "password123")
    private String password;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "이름 (최대 100자)", example = "홍길동")
    private String name;

    @NotBlank
    @Size(max = 20)
    @Schema(description = "전화번호 (최대 20자)", example = "010-1234-5678")
    private String phoneNumber;

    @NotNull
    @Schema(description = "소속 매장 ID", example = "1")
    private Long storeId;

    @NotNull
    @Schema(description = "역할")
    private UserRole role;
}