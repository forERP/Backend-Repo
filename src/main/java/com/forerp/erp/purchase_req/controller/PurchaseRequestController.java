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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PurchaseRequest", description = "Purchase request API")
@RestController
@RequestMapping("/api/purchase-requests")
public class PurchaseRequestController {

    private final PurchaseRequestService purchaseRequestService;

    public PurchaseRequestController(PurchaseRequestService purchaseRequestService) {
        this.purchaseRequestService = purchaseRequestService;
    }

    @Operation(summary = "Create purchase request")
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

    @Operation(summary = "Get purchase request")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PurchaseRequestResponse.class)))
    @GetMapping("/{purchaseRequestId}")
    public ResponseEntity<PurchaseRequestResponse> get(@PathVariable Long purchaseRequestId) {
        PurchaseRequest pr = purchaseRequestService.get(purchaseRequestId);
        return ResponseEntity.ok(PurchaseRequestResponse.from(pr));
    }

    @Operation(summary = "List purchase requests")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PurchaseRequestListResponse.class)))
    @GetMapping
    public ResponseEntity<PurchaseRequestListResponse> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String storeName,
            @RequestParam(required = false) String storeCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        return ResponseEntity.ok(purchaseRequestService.list(storeId, storeName, storeCode, status, from, to, page, size));
    }

    @Operation(summary = "Create purchase order draft")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class)))
    @PostMapping("/{purchaseRequestId}/draft-order")
    public ResponseEntity<PurchaseOrderResponse> createDraftOrder(
            @PathVariable Long purchaseRequestId,
            @Valid @RequestBody PurchaseRequestApproveRequest request,
            @AuthenticationPrincipal User actor
    ) {
        PurchaseOrder po = purchaseRequestService.createDraftOrder(purchaseRequestId, request, actor);
        return ResponseEntity.ok(PurchaseOrderResponse.from(po));
    }

    @Operation(summary = "Get purchase order draft")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class)))
    @ApiResponse(responseCode = "404", description = "Draft not found")
    @GetMapping("/{purchaseRequestId}/draft-order")
    public ResponseEntity<PurchaseOrderResponse> getDraftOrder(@PathVariable Long purchaseRequestId) {
        return purchaseRequestService.getDraftOrder(purchaseRequestId)
                .map(po -> ResponseEntity.ok(PurchaseOrderResponse.from(po)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Approve purchase request")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class)))
    @PostMapping("/{purchaseRequestId}/approve")
    public ResponseEntity<PurchaseOrderResponse> approve(@PathVariable Long purchaseRequestId) {
        PurchaseOrder po = purchaseRequestService.approve(purchaseRequestId);
        return ResponseEntity.ok(PurchaseOrderResponse.from(po));
    }

    @Operation(summary = "Reject purchase request")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PurchaseRequestResponse.class)))
    @PostMapping("/{purchaseRequestId}/reject")
    public ResponseEntity<PurchaseRequestResponse> reject(@PathVariable Long purchaseRequestId) {
        PurchaseRequest pr = purchaseRequestService.reject(purchaseRequestId);
        return ResponseEntity.ok(PurchaseRequestResponse.from(pr));
    }
}