package com.forerp.erp.purchase_req.controller;

import com.forerp.erp.purchase_order.domain.PurchaseOrder;
import com.forerp.erp.purchase_order.dto.PurchaseOrderResponse;
import com.forerp.erp.purchase_req.domain.PurchaseRequest;
import com.forerp.erp.purchase_req.dto.PurchaseRequestApproveRequest;
import com.forerp.erp.purchase_req.dto.PurchaseRequestCreateRequest;
import com.forerp.erp.purchase_req.dto.PurchaseRequestListResponse;
import com.forerp.erp.purchase_req.dto.PurchaseRequestResponse;
import com.forerp.erp.purchase_req.service.PurchaseRequestService;
import com.forerp.erp.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "PurchaseRequest", description = "발주요청(지점→본사) API (관리자)")
@RestController
@RequestMapping("/api/purchase-requests")
public class PurchaseRequestController {

    private final PurchaseRequestService purchaseRequestService;

    public PurchaseRequestController(PurchaseRequestService purchaseRequestService) {
        this.purchaseRequestService = purchaseRequestService;
    }

    @Operation(summary = "발주요청 생성", description = "STORE_ADMIN이 발주요청 문서를 생성합니다.")
    @ApiResponse(responseCode = "201", description = "Created",
            content = @Content(schema = @Schema(implementation = PurchaseRequestResponse.class)))
    @PostMapping
    public ResponseEntity<PurchaseRequestResponse> create(
            @Valid @RequestBody PurchaseRequestCreateRequest request,
            @AuthenticationPrincipal User actor
    ) {
        PurchaseRequest pr = purchaseRequestService.create(request, actor);
        return ResponseEntity.status(201).body(PurchaseRequestResponse.from(pr));
    }

    @Operation(summary = "발주요청 단건 조회")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PurchaseRequestResponse.class)))
    @GetMapping("/{purchaseRequestId}")
    public ResponseEntity<PurchaseRequestResponse> get(@PathVariable Long purchaseRequestId) {
        PurchaseRequest pr = purchaseRequestService.get(purchaseRequestId);
        return ResponseEntity.ok(PurchaseRequestResponse.from(pr));
    }

    @Operation(summary = "발주요청 목록 조회/검색")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PurchaseRequestListResponse.class)))
    @GetMapping
    public ResponseEntity<PurchaseRequestListResponse> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        return ResponseEntity.ok(purchaseRequestService.list(storeId, status, from, to, page, size));
    }

    @Operation(summary = "발주요청 승인(=발주 자동 생성)", description = "HQ_ADMIN이 요청을 승인하고 PurchaseOrder를 생성합니다.")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class)))
    @PostMapping("/{purchaseRequestId}/approve")
    public ResponseEntity<PurchaseOrderResponse> approve(
            @PathVariable Long purchaseRequestId,
            @Valid @RequestBody PurchaseRequestApproveRequest request
    ) {
        PurchaseOrder po = purchaseRequestService.approve(purchaseRequestId, request);
        return ResponseEntity.ok(PurchaseOrderResponse.from(po));
    }

    @Operation(summary = "발주요청 반려")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PurchaseRequestResponse.class)))
    @PostMapping("/{purchaseRequestId}/reject")
    public ResponseEntity<PurchaseRequestResponse> reject(@PathVariable Long purchaseRequestId) {
        PurchaseRequest pr = purchaseRequestService.reject(purchaseRequestId);
        return ResponseEntity.ok(PurchaseRequestResponse.from(pr));
    }
}