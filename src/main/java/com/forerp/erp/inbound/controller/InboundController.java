package com.forerp.erp.inbound.controller;

import com.forerp.erp.inbound.domain.Inbound;
import com.forerp.erp.inbound.dto.InboundCreateRequest;
import com.forerp.erp.inbound.dto.InboundListResponse;
import com.forerp.erp.inbound.dto.InboundResponse;
import com.forerp.erp.inbound.service.InboundService;
import com.forerp.erp.shipment.domain.Shipment;
import com.forerp.erp.shipment.dto.ShipmentDepartRequest;
import com.forerp.erp.shipment.dto.ShipmentResponse;
import com.forerp.erp.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Inbound", description = "입고(거래처 → 지점) 관리 API (관리자 전용)")
@RestController
@RequestMapping("/api/inbounds")
@RequiredArgsConstructor
public class InboundController {

    private final InboundService inboundService;

    @Operation(summary = "입고 생성", description = "발주 기반 입고 문서 생성 + Shipment(READY) 자동 생성")
    @ApiResponse(responseCode = "201", description = "Created",
            content = @Content(schema = @Schema(implementation = InboundResponse.class)))
    @PostMapping
    public ResponseEntity<InboundResponse> createInbound(@Valid @RequestBody InboundCreateRequest request) {
        Inbound inbound = inboundService.createInbound(request);
        return ResponseEntity.status(201).body(InboundResponse.from(inbound));
    }

    @Operation(summary = "입고 배송 출발(송장 입력)", description = "Shipment READY → SHIPPING")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = ShipmentResponse.class)))
    @PostMapping("/{inboundId}/shipment/depart")
    public ResponseEntity<ShipmentResponse> departShipment(
            @PathVariable Long inboundId,
            @Valid @RequestBody ShipmentDepartRequest request
    ) {
        Shipment shipment = inboundService.departShipment(inboundId, request.getCarrier(), request.getTrackingNumber());
        return ResponseEntity.ok(ShipmentResponse.from(shipment));
    }

    @Operation(summary = "입고 확정", description = "배송 도착 처리 + 재고 증가 + Inbound CONFIRMED + PurchaseOrder markReceived")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = InboundResponse.class)))
    @PostMapping("/{inboundId}/confirm")
    public ResponseEntity<InboundResponse> confirmInbound(
            @PathVariable Long inboundId,
            @AuthenticationPrincipal User actor
    ) {
        Inbound inbound = inboundService.confirmInbound(inboundId, actor);
        return ResponseEntity.ok(InboundResponse.from(inbound));
    }

    @Operation(summary = "입고 취소", description = "배송 출발 전(Shipment READY)까지만 취소 가능")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = InboundResponse.class)))
    @PostMapping("/{inboundId}/cancel")
    public ResponseEntity<InboundResponse> cancelInbound(@PathVariable Long inboundId) {
        Inbound inbound = inboundService.cancelInbound(inboundId);
        return ResponseEntity.ok(InboundResponse.from(inbound));
    }

    @Operation(summary = "입고 단건 조회")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = InboundResponse.class)))
    @GetMapping("/{inboundId}")
    public ResponseEntity<InboundResponse> getInbound(@PathVariable Long inboundId) {
        Inbound inbound = inboundService.getInbound(inboundId);
        return ResponseEntity.ok(InboundResponse.from(inbound));
    }


    @Operation(summary = "입고 목록 조회/검색",
            description = """
                - storeId/status/from/to는 선택
                - from/to 형식: yyyy-MM-dd
                - to는 '포함' 조건(내부적으로 to+1일 미만으로 조회)
                """)
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = InboundListResponse.class)))
    @GetMapping
    public ResponseEntity<InboundListResponse> listInbounds(
            @Parameter(description = "매장 ID(선택)", example = "1")
            @RequestParam(required = false) Long storeId,

            @Parameter(description = "매장명(선택, 부분일치)", example = "강남")
            @RequestParam(required = false) String storeName,

            @Parameter(description = "매장코드(선택, 부분일치)", example = "ST")
            @RequestParam(required = false) String storeCode,

            @Parameter(description = "입고 상태(선택): CREATED/CONFIRMED/CANCELED", example = "CREATED")
            @RequestParam(required = false) String status,

            @Parameter(description = "조회 시작일(선택), yyyy-MM-dd", example = "2026-02-01")
            @RequestParam(required = false) String from,

            @Parameter(description = "조회 종료일(선택), yyyy-MM-dd", example = "2026-02-28")
            @RequestParam(required = false) String to,

            @Parameter(description = "페이지(0부터)", example = "0")
            @Min(0)
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20")
            @Min(1) @Max(100)
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(inboundService.listInbounds(storeId, storeName, storeCode, status, from, to, page, size));
    }
}
