package com.forerp.erp.shipment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TrackerWebhookRequest {
    private String carrierId;
    private String trackingNumber;
}
