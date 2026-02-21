package com.forerp.erp.returns.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReturnProcessRequest {

    @NotNull
    private Long orderId;

    @NotBlank
    private String reason;

    @NotNull
    private Boolean discardStock = Boolean.FALSE;
}

