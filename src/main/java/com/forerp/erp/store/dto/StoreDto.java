package com.forerp.erp.store.dto;

import com.forerp.erp.store.domain.StoreStatus;
import com.forerp.erp.store.domain.StoreType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class StoreDto {

    @Schema(description = "매장 생성 요청")
    @Getter
    @NoArgsConstructor
    public static class CreateRequest {
        @Schema(description = "매장명", example = "강남점", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        private String name;

        @Schema(description = "전화번호", example = "02-1234-5678")
        private String phone;

        @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
        private String address;

        @Schema(description = "위도", example = "37.5025")
        private Double latitude;

        @Schema(description = "경도", example = "127.0276")
        private Double longitude;

        @Schema(description = "매장 유형 (HQ/STORE)", example = "STORE")
        private StoreType type;
    }

    @Schema(description = "매장 수정 요청 (변경할 필드만 전송)")
    @Getter
    @NoArgsConstructor
    public static class UpdateRequest {
        @Schema(description = "매장명", example = "강남점")
        private String name;

        @Schema(description = "전화번호", example = "02-1234-5678")
        private String phone;

        @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
        private String address;

        @Schema(description = "위도", example = "37.5025")
        private Double latitude;

        @Schema(description = "경도", example = "127.0276")
        private Double longitude;
    }

    @Schema(description = "매장 상태 변경 요청")
    @Getter
    @NoArgsConstructor
    public static class UpdateStatusRequest {
        @Schema(description = "변경할 상태 (OPEN/INACTIVE/CLOSED)", example = "OPEN")
        private StoreStatus status;
    }

    @Schema(description = "매장 응답")
    @Getter
    @AllArgsConstructor
    public static class Response {
        @Schema(description = "매장 ID", example = "1")
        private Long id;

        @Schema(description = "매장 코드", example = "S0001")
        private String code;

        @Schema(description = "매장명", example = "강남점")
        private String name;

        @Schema(description = "전화번호", example = "02-1234-5678")
        private String phone;

        @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
        private String address;

        @Schema(description = "위도", example = "37.5025")
        private Double latitude;

        @Schema(description = "경도", example = "127.0276")
        private Double longitude;

        @Schema(description = "매장 유형", example = "STORE")
        private StoreType type;

        @Schema(description = "매장 상태", example = "OPEN")
        private StoreStatus status;

        @Schema(description = "생성 일시")
        private LocalDateTime createdAt;
    }
}