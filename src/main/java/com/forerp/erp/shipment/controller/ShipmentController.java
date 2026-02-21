package com.forerp.erp.shipment.controller;

import com.forerp.erp.shipment.dto.ShipmentCarrierResponse;
import com.forerp.erp.shipment.dto.ShipmentListResponse;
import com.forerp.erp.shipment.dto.ShipmentTrackingResponse;
import com.forerp.erp.shipment.dto.TrackerWebhookRequest;
import com.forerp.erp.shipment.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Shipment", description = "입고/출고 배송 및 송장 추적 API")
@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @Operation(summary = "택배사 목록 조회")
    @GetMapping("/carriers")
    public ResponseEntity<List<ShipmentCarrierResponse>> listCarriers(
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) Integer size
    ) {
        return ResponseEntity.ok(shipmentService.listCarriers(searchText, size));
    }

    @Operation(summary = "배송 목록 조회")
    @GetMapping
    public ResponseEntity<ShipmentListResponse> listShipments(
            @RequestParam(required = false) String flowType,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String shipmentStatus,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(shipmentService.listShipments(
                flowType,
                storeId,
                warehouseId,
                shipmentStatus,
                from,
                to,
                page,
                size
        ));
    }

    @Operation(summary = "배송 추적 조회", description = "sync=true 시 트래커 상태를 동기화하여 내부 상태에 반영합니다.")
    @GetMapping("/{shipmentId}/tracking")
    public ResponseEntity<ShipmentTrackingResponse> getTracking(
            @PathVariable Long shipmentId,
            @RequestParam(defaultValue = "true") boolean sync
    ) {
        return ResponseEntity.ok(shipmentService.getTracking(shipmentId, sync));
    }

    @Operation(summary = "Tracker webhook", description = "tracker.delivery webhook callback endpoint")
    @PostMapping("/webhook/tracker")
    public ResponseEntity<Void> trackerWebhook(
            @RequestParam(required = false) String token,
            @RequestBody(required = false) TrackerWebhookRequest request
    ) {
        String carrierId = request == null ? null : request.getCarrierId();
        String trackingNumber = request == null ? null : request.getTrackingNumber();

        try {
            shipmentService.handleTrackerWebhook(token, carrierId, trackingNumber);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok().build();
    }
}
