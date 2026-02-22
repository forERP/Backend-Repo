package com.forerp.erp.shipment.controller;

import com.forerp.erp.common.exception.ApiErrorResponse;
import com.forerp.erp.shipment.dto.ShipmentCarrierResponse;
import com.forerp.erp.shipment.dto.ShipmentListResponse;
import com.forerp.erp.shipment.dto.ShipmentTrackingResponse;
import com.forerp.erp.shipment.dto.TrackerWebhookRequest;
import com.forerp.erp.shipment.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "배송", description = "입고/출고 배송 추적 및 운송장 관리 API")
@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @Operation(summary = "택배사 목록 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = ShipmentCarrierResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/carriers")
    public ResponseEntity<List<ShipmentCarrierResponse>> listCarriers(
            @Parameter(description = "검색어 (선택)") @RequestParam(required = false) String searchText,
            @Parameter(description = "최대 반환 개수 (선택)") @RequestParam(required = false) Integer size
    ) {
        return ResponseEntity.ok(shipmentService.listCarriers(searchText, size));
    }

    @Operation(summary = "배송 목록 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = ShipmentListResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ShipmentListResponse> listShipments(
            @Parameter(description = "흐름 유형 (INBOUND/OUTBOUND)") @RequestParam(required = false) String flowType,
            @Parameter(description = "매장 ID (선택)") @RequestParam(required = false) Long storeId,
            @Parameter(description = "창고 ID (선택)") @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "배송 상태 (선택): READY/SHIPPING/ARRIVED") @RequestParam(required = false) String shipmentStatus,
            @Parameter(description = "조회 시작일 (yyyy-MM-dd)") @RequestParam(required = false) String from,
            @Parameter(description = "조회 종료일 (yyyy-MM-dd)") @RequestParam(required = false) String to,
            @Parameter(description = "페이지 (0부터)", example = "0") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "페이지 크기 (최대 100)", example = "20") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(shipmentService.listShipments(
                flowType, storeId, warehouseId, shipmentStatus, from, to, page, size));
    }

    @Operation(summary = "배송 추적 조회", description = "sync=true 이면 외부 택배사 상태를 동기화한 후 최신 상태를 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = ShipmentTrackingResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "배송 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{shipmentId}/tracking")
    public ResponseEntity<ShipmentTrackingResponse> getTracking(
            @Parameter(description = "배송 ID", example = "1") @PathVariable Long shipmentId,
            @Parameter(description = "동기화 여부 (기본: true)") @RequestParam(defaultValue = "true") boolean sync
    ) {
        return ResponseEntity.ok(shipmentService.getTracking(shipmentId, sync));
    }

    @Operation(summary = "Tracker 웹훅", description = "tracker.delivery 웹훅 콜백 엔드포인트")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "처리 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/webhook/tracker")
    public ResponseEntity<Void> trackerWebhook(
            @Parameter(description = "웹훅 토큰") @RequestParam(required = false) String token,
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