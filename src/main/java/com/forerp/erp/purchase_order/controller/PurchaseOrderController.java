package com.forerp.erp.purchase_order.controller;

import com.forerp.erp.purchase_order.domain.PurchaseOrder;
import com.forerp.erp.purchase_order.dto.PurchaseOrderDraftUpdateRequest;
import com.forerp.erp.purchase_order.dto.PurchaseOrderListResponse;
import com.forerp.erp.purchase_order.dto.PurchaseOrderResponse;
import com.forerp.erp.purchase_order.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PurchaseOrder", description = "발주 API (관리자)")
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
            @RequestParam(required = false) String storeKeyword,
            @RequestParam(required = false) String storeName,
            @RequestParam(required = false) String storeCode,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String createdFrom,
            @RequestParam(required = false) String createdTo,
            @RequestParam(required = false) String orderedFrom,
            @RequestParam(required = false) String orderedTo,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        return ResponseEntity.ok(
                purchaseOrderService.list(
                        storeId,
                        storeKeyword,
                        storeName,
                        storeCode,
                        warehouseId,
                        supplierId,
                        supplierName,
                        status,
                        createdFrom,
                        createdTo,
                        orderedFrom,
                        orderedTo,
                        from,
                        to,
                        page,
                        size
                )
        );
    }

    @Operation(summary = "발주 확정(ORDERED)")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class)))
    @PostMapping("/{purchaseOrderId}/order")
    public ResponseEntity<PurchaseOrderResponse> order(@PathVariable Long purchaseOrderId) {
        PurchaseOrder po = purchaseOrderService.order(purchaseOrderId);
        return ResponseEntity.ok(PurchaseOrderResponse.from(po));
    }

    @Operation(summary = "작성 단계 발주서 수정")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class)))
    @PutMapping("/{purchaseOrderId}/draft")
    public ResponseEntity<PurchaseOrderResponse> updateDraft(
            @PathVariable Long purchaseOrderId,
            @Valid @RequestBody PurchaseOrderDraftUpdateRequest request
    ) {
        PurchaseOrder po = purchaseOrderService.updateDraft(purchaseOrderId, request);
        return ResponseEntity.ok(PurchaseOrderResponse.from(po));
    }

    @Operation(summary = "작성 단계 발주서 삭제")
    @ApiResponse(responseCode = "204", description = "No Content")
    @DeleteMapping("/{purchaseOrderId}/draft")
    public ResponseEntity<Void> deleteDraft(@PathVariable Long purchaseOrderId) {
        purchaseOrderService.deleteDraft(purchaseOrderId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "발주서 엑셀 다운로드")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/{purchaseOrderId}/document")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long purchaseOrderId) {
        byte[] file = purchaseOrderService.exportDocument(purchaseOrderId);
        String filename = purchaseOrderService.buildDocumentDownloadFilename(purchaseOrderId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        ));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(filename)
                        .build()
        );

        return ResponseEntity.ok().headers(headers).body(file);
    }

    @Operation(summary = "발주 취소(CANCELLED)")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class)))
    @PostMapping("/{purchaseOrderId}/cancel")
    public ResponseEntity<PurchaseOrderResponse> cancel(@PathVariable Long purchaseOrderId) {
        PurchaseOrder po = purchaseOrderService.cancel(purchaseOrderId);
        return ResponseEntity.ok(PurchaseOrderResponse.from(po));
    }
}
