package com.forerp.erp.store.dto;

import com.forerp.erp.store.domain.StoreStatus;
import com.forerp.erp.store.domain.StoreType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class StoreDto {

    @Getter
    @NoArgsConstructor
    public static class CreateRequest{
        @NotBlank
        private String name;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateStatusRequest{
        private StoreStatus status;
    }

    @Getter
    @AllArgsConstructor
    public static class Response{
        private Long id;
        private String code;
        private String name;
        private StoreType type;
        private StoreStatus status;
        private LocalDateTime createdAt;
    }
}
