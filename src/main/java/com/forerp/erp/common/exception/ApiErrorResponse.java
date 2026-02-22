package com.forerp.erp.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "공통 에러 응답")
public class ApiErrorResponse {

    @Schema(description = "HTTP 상태 코드", example = "400")
    private int status;

    @Schema(description = "에러 코드", example = "VALIDATION_ERROR")
    private String code;

    @Schema(description = "에러 메시지", example = "입력값이 올바르지 않습니다.")
    private String message;
}