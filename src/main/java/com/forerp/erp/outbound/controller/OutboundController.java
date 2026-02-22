package com.forerp.erp.outbound.controller;

import com.forerp.erp.common.exception.ApiErrorResponse;
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
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "출고", description = "출고 (창고 → 매장) 관리 API")
@RestController
@RequestMapping("/api/outbounds")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class OutboundController {

    private final OutboundService outboundService;

    @Operation(summary = "출고 생성")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 성공",
                content = @Content(schema = @Schema(implementation = OutboundResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<OutboundResponse> createOutbound(@Valid @RequestBody OutboundCreateRequest request) {
        Outbound outbound = outboundService.createOutbound(request);
        return ResponseEntity.status(201).body(OutboundResponse.from(outbound));
    }

    @Operation(summary = "출고 확정 (운송장 등록)", description = "출고 확정 + Shipment SHIPPING 처리")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "확정 성공",
                content = @Content(schema = @Schema(implementation = OutboundResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "출고 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{outboundId}/confirm")
    public ResponseEntity<OutboundResponse> confirmOutbound(
            @Parameter(description = "출고 ID", example = "1") @PathVariable Long outboundId,
            @Valid @RequestBody OutboundConfirmRequest request,
            @AuthenticationPrincipal User actor
    ) {
        Outbound outbound = outboundService.confirmOutbound(
                outboundId, actor,
                request.getCarrierCode(),
                request.getCarrier(),
                request.getTrackingNumber()
        );
        return ResponseEntity.ok(OutboundResponse.from(outbound));
    }

    @Operation(summary = "출고 배송 도착 처리")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "처리 성공",
                content = @Content(schema = @Schema(implementation = OutboundResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "출고 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{outboundId}/shipment/arrive")
    public ResponseEntity<OutboundResponse> arriveShipment(
            @Parameter(description = "출고 ID", example = "1") @PathVariable Long outboundId) {
        Outbound outbound = outboundService.arriveOutbound(outboundId);
        return ResponseEntity.ok(OutboundResponse.from(outbound));
    }

    @Operation(summary = "출고 취소")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "취소 성공",
                content = @Content(schema = @Schema(implementation = OutboundResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "출고 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{outboundId}/cancel")
    public ResponseEntity<OutboundResponse> cancelOutbound(
            @Parameter(description = "출고 ID", example = "1") @PathVariable Long outboundId) {
        Outbound outbound = outboundService.cancelOutbound(outboundId);
        return ResponseEntity.ok(OutboundResponse.from(outbound));
    }

    @Operation(summary = "출고 단건 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = OutboundResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "출고 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{outboundId}")
    public ResponseEntity<OutboundResponse> getOutbound(
            @Parameter(description = "출고 ID", example = "1") @PathVariable Long outboundId) {
        Outbound outbound = outboundService.getOutbound(outboundId);
        return ResponseEntity.ok(OutboundResponse.from(outbound));
    }

    @Operation(summary = "출고 목록 조회/검색")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = OutboundListResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<OutboundListResponse> listOutbounds(
            @Parameter(description = "매장 ID (선택)", example = "1") @RequestParam(required = false) Long storeId,
            @Parameter(description = "매장 통합 검색어 (선택)") @RequestParam(required = false) String storeKeyword,
            @Parameter(description = "매장명 (선택)") @RequestParam(required = false) String storeName,
            @Parameter(description = "매장코드 (선택)") @RequestParam(required = false) String storeCode,
            @Parameter(description = "창고 ID (선택)", example = "2") @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "출고 상태 (선택): CREATED/CONFIRMED/ARRIVED/CANCELED", example = "CREATED") @RequestParam(required = false) String status,
            @Parameter(description = "Shipment 상태 (선택): READY/SHIPPING/ARRIVED", example = "SHIPPING") @RequestParam(required = false) String shipmentStatus,
            @Parameter(description = "조회 시작일 (yyyy-MM-dd)", example = "2026-02-01") @RequestParam(required = false) String from,
            @Parameter(description = "조회 종료일 (yyyy-MM-dd)", example = "2026-02-28") @RequestParam(required = false) String to,
            @Parameter(description = "페이지 (0부터)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(outboundService.listOutbounds(
                storeId, storeKeyword, storeName, storeCode,
                warehouseId, status, shipmentStatus, from, to, page, size
        ));
    }
}