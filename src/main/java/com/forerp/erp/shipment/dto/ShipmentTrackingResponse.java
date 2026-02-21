package com.forerp.erp.shipment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentTrackingResponse {

    private Long shipmentId;
    private String flowType;
    private String localShipmentStatus;
    private String carrierCode;
    private String carrier;
    private String trackingNumber;
    private String trackerStatusCode;
    private String trackerStatusName;
    private String trackerEventTime;
    private String trackerEventLocation;
    private String trackerEventDescription;
    private boolean delivered;
    private boolean localStatusChanged;
    private String message;
    private List<TrackingEventItem> events;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackingEventItem {
        private String statusCode;
        private String statusName;
        private String time;
        private String location;
        private String description;
    }
}
