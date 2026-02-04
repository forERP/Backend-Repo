package com.forerp.erp.shipment.dto;

import com.forerp.erp.shipment.domain.Shipment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {
    private Long shipmentId;
    private String status; // READY/SHIPPING/ARRIVED
    private String carrier;
    private String trackingNumber;
    private LocalDateTime departedAt;
    private LocalDateTime arrivedAt;

    public static ShipmentResponse from(Shipment s) {
        return new ShipmentResponse(
                s.getId(),
                s.getStatus().name(),
                s.getCarrier(),
                s.getTrackingNumber(),
                s.getDepartedAt(),
                s.getArrivedAt()
        );
    }
}