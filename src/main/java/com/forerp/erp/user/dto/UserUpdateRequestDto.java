package com.forerp.erp.user.dto;

import com.forerp.erp.user.domain.UserRole;
import com.forerp.erp.user.domain.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "사용자 정보 수정 요청 (null 필드는 수정 제외)")
public class UserUpdateRequestDto {

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;

    @Schema(description = "비밀번호 (변경 시에만 입력)")
    private String password;

    @Schema(description = "역할")
    private UserRole role;

    @Schema(description = "계정 상태")
    private UserStatus status;

    @Schema(description = "소속 매장 ID", example = "1")
    private Long storeId;
}