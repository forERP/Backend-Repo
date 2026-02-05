package com.forerp.erp.purchase_order.controller;

import com.forerp.erp.purchase_order.domain.PurchaseOrder;
import com.forerp.erp.purchase_order.dto.PurchaseOrderListResponse;
import com.forerp.erp.purchase_order.dto.PurchaseOrderResponse;
import com.forerp.erp.purchase_order.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "PurchaseOrder", description = "발주(본사→거래처) API (관리자)")
@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @Operation(summary = "발주 단건 조회")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class)))
    @GetMapping("/{purchaseOrderId}")
    public ResponseEntity<PurchaseOrderResponse> get(@PathVariable Long purchaseOrderId) {
        PurchaseOrder po = purchaseOrderService.get(purchaseOrderId);
        return ResponseEntity.ok(PurchaseOrderResponse.from(po));
    }

    @Operation(summary = "발주 목록 조회/검색")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PurchaseOrderListResponse.class)))
    @GetMapping
    public ResponseEntity<PurchaseOrderListResponse> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        return ResponseEntity.ok(purchaseOrderService.list(storeId, warehouseId, status, from, to, page, size));
    }

    @Operation(summary = "발주 확정(ORDERED)", description = "PurchaseOrder.CREATED -> ORDERED")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class)))
    @PostMapping("/{purchaseOrderId}/order")
    public ResponseEntity<PurchaseOrderResponse> order(@PathVariable Long purchaseOrderId) {
        PurchaseOrder po = purchaseOrderService.order(purchaseOrderId);
        return ResponseEntity.ok(PurchaseOrderResponse.from(po));
    }

    @Operation(summary = "발주 취소(CANCELLED)", description = "ORDERED 전까지만 취소 가능")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class)))
    @PostMapping("/{purchaseOrderId}/cancel")
    public ResponseEntity<PurchaseOrderResponse> cancel(@PathVariable Long purchaseOrderId) {
        PurchaseOrder po = purchaseOrderService.cancel(purchaseOrderId);
        return ResponseEntity.ok(PurchaseOrderResponse.from(po));
    }
}