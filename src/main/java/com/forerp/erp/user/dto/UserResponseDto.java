package com.forerp.erp.user.dto;

import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.domain.UserRole;
import com.forerp.erp.user.domain.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "사용자 응답")
public class UserResponseDto {

    @Schema(description = "사용자 ID", example = "1")
    private final Long id;

    @Schema(description = "로그인 ID", example = "user01")
    private final String loginId;

    @Schema(description = "사원번호", example = "EMP001")
    private final String employeeCode;

    @Schema(description = "이름", example = "홍길동")
    private final String name;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private final String phoneNumber;

    @Schema(description = "소속 매장 ID", example = "1")
    private final Long storeId;

    @Schema(description = "매장 코드", example = "STORE001")
    private final String storeCode;

    @Schema(description = "매장 이름", example = "강남점")
    private final String storeName;

    @Schema(description = "역할")
    private final UserRole role;

    @Schema(description = "계정 상태")
    private final UserStatus status;

    @Schema(description = "가입일시")
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