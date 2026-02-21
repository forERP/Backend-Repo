package com.forerp.erp.outbound.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutboundConfirmRequest {
    private String carrierCode;

    @NotBlank
    private String carrier;

    @NotBlank
    private String trackingNumber;
}
