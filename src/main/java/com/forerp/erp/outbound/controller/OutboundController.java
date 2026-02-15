package com.forerp.erp.outbound.controller;

import com.forerp.erp.outbound.domain.Outbound;
import com.forerp.erp.outbound.dto.OutboundConfirmRequest;
import com.forerp.erp.outbound.dto.OutboundCreateRequest;
import com.forerp.erp.outbound.dto.OutboundListResponse;
import com.forerp.erp.outbound.dto.OutboundResponse;
import com.forerp.erp.outbound.service.OutboundService;
import com.forerp.erp.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Outbound", description = "Outbound API")
@RestController
@RequestMapping("/api/outbounds")
@RequiredArgsConstructor
public class OutboundController {

    private final OutboundService outboundService;

    @Operation(summary = "Create outbound")
    @ApiResponse(responseCode = "201", description = "Created",
            content = @Content(schema = @Schema(implementation = OutboundResponse.class)))
    @PostMapping
    public ResponseEntity<OutboundResponse> createOutbound(@Valid @RequestBody OutboundCreateRequest request) {
        Outbound outbound = outboundService.createOutbound(request);
        return ResponseEntity.status(201).body(OutboundResponse.from(outbound));
    }

    @Operation(summary = "Confirm outbound")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = OutboundResponse.class)))
    @PostMapping("/{outboundId}/confirm")
    public ResponseEntity<OutboundResponse> confirmOutbound(
            @PathVariable Long outboundId,
            @Valid @RequestBody OutboundConfirmRequest request,
            @AuthenticationPrincipal User actor
    ) {
        Outbound outbound = outboundService.confirmOutbound(
                outboundId,
                actor,
                request.getCarrier(),
                request.getTrackingNumber()
        );
        return ResponseEntity.ok(OutboundResponse.from(outbound));
    }

    @Operation(summary = "Mark shipment arrived")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = OutboundResponse.class)))
    @PostMapping("/{outboundId}/shipment/arrive")
    public ResponseEntity<OutboundResponse> arriveShipment(@PathVariable Long outboundId) {
        Outbound outbound = outboundService.arriveOutbound(outboundId);
        return ResponseEntity.ok(OutboundResponse.from(outbound));
    }

    @Operation(summary = "Cancel outbound")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = OutboundResponse.class)))
    @PostMapping("/{outboundId}/cancel")
    public ResponseEntity<OutboundResponse> cancelOutbound(@PathVariable Long outboundId) {
        Outbound outbound = outboundService.cancelOutbound(outboundId);
        return ResponseEntity.ok(OutboundResponse.from(outbound));
    }

    @Operation(summary = "Get outbound")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = OutboundResponse.class)))
    @GetMapping("/{outboundId}")
    public ResponseEntity<OutboundResponse> getOutbound(@PathVariable Long outboundId) {
        Outbound outbound = outboundService.getOutbound(outboundId);
        return ResponseEntity.ok(OutboundResponse.from(outbound));
    }

    @Operation(summary = "List outbounds")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = OutboundListResponse.class)))
    @GetMapping
    public ResponseEntity<OutboundListResponse> listOutbounds(
            @Parameter(description = "Store ID", example = "1")
            @RequestParam(required = false) Long storeId,

            @Parameter(description = "Store name", example = "Gangnam")
            @RequestParam(required = false) String storeName,

            @Parameter(description = "Store code", example = "S0001")
            @RequestParam(required = false) String storeCode,

            @Parameter(description = "Warehouse ID", example = "2")
            @RequestParam(required = false) Long warehouseId,

            @Parameter(description = "Outbound status: CREATED/CONFIRMED/ARRIVED/CANCELED", example = "CREATED")
            @RequestParam(required = false) String status,

            @Parameter(description = "Shipment status: READY/SHIPPING/ARRIVED", example = "SHIPPING")
            @RequestParam(required = false) String shipmentStatus,

            @Parameter(description = "Start date (yyyy-MM-dd)", example = "2026-02-01")
            @RequestParam(required = false) String from,

            @Parameter(description = "End date (yyyy-MM-dd)", example = "2026-02-28")
            @RequestParam(required = false) String to,

            @Parameter(description = "Page (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(outboundService.listOutbounds(
                storeId,
                storeName,
                storeCode,
                warehouseId,
                status,
                shipmentStatus,
                from,
                to,
                page,
                size
        ));
    }
}
