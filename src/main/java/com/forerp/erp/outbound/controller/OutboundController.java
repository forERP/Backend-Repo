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

@Tag(name = "Outbound", description = "출고(지점 → 소비자) 처리 API (POS 포함)")
@RestController
@RequestMapping("/api/outbounds")
@RequiredArgsConstructor
public class OutboundController {

    private final OutboundService outboundService;

    @Operation(summary = "출고 생성", description = "출고 문서 생성 + Shipment(READY) 자동 생성")
    @ApiResponse(responseCode = "201", description = "Created",
            content = @Content(schema = @Schema(implementation = OutboundResponse.class)))
    @PostMapping
    public ResponseEntity<OutboundResponse> createOutbound(@Valid @RequestBody OutboundCreateRequest request) {
        Outbound outbound = outboundService.createOutbound(request);
        return ResponseEntity.status(201).body(OutboundResponse.from(outbound));
    }

    @Operation(
            summary = "출고 확정(배송 출발 + 재고 차감 + 출고 확정)",
            description = "Shipment.depart(READY→SHIPPING) 후 재고 차감 및 Outbound CONFIRMED"
    )
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

    @Operation(summary = "출고 취소", description = "배송 출발 전(Shipment READY)까지만 취소 가능")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = OutboundResponse.class)))
    @PostMapping("/{outboundId}/cancel")
    public ResponseEntity<OutboundResponse> cancelOutbound(@PathVariable Long outboundId) {
        Outbound outbound = outboundService.cancelOutbound(outboundId);
        return ResponseEntity.ok(OutboundResponse.from(outbound));
    }

    @Operation(summary = "출고 단건 조회")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = OutboundResponse.class)))
    @GetMapping("/{outboundId}")
    public ResponseEntity<OutboundResponse> getOutbound(@PathVariable Long outboundId) {
        Outbound outbound = outboundService.getOutbound(outboundId);
        return ResponseEntity.ok(OutboundResponse.from(outbound));
    }

    @Operation(summary = "출고 목록 조회/검색",
            description = """
            - storeId/warehouseId/status/from/to는 선택
            - from/to 형식: yyyy-MM-dd
            - to는 '포함' 조건(내부적으로 to+1일 미만으로 조회)
            """)
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = OutboundListResponse.class)))
    @GetMapping
    public ResponseEntity<OutboundListResponse> listOutbounds(
            @Parameter(description = "매장 ID(선택)", example = "1")
            @RequestParam(required = false) Long storeId,

            @Parameter(description = "창고 ID(선택)", example = "2")
            @RequestParam(required = false) Long warehouseId,

            @Parameter(description = "출고 상태(선택): CREATED/CONFIRMED/CANCELED", example = "CREATED")
            @RequestParam(required = false) String status,

            @Parameter(description = "조회 시작일(선택), yyyy-MM-dd", example = "2026-02-01")
            @RequestParam(required = false) String from,

            @Parameter(description = "조회 종료일(선택), yyyy-MM-dd", example = "2026-02-28")
            @RequestParam(required = false) String to,

            @Parameter(description = "페이지(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(outboundService.listOutbounds(storeId, warehouseId, status, from, to, page, size));
    }
}