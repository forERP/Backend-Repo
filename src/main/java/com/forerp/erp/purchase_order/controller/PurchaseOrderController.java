package com.forerp.erp.purchase_order.controller;

import com.forerp.erp.common.exception.ApiErrorResponse;
import com.forerp.erp.purchase_order.domain.PurchaseOrder;
import com.forerp.erp.purchase_order.dto.PurchaseOrderDraftUpdateRequest;
import com.forerp.erp.purchase_order.dto.PurchaseOrderListResponse;
import com.forerp.erp.purchase_order.dto.PurchaseOrderResponse;
import com.forerp.erp.purchase_order.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "발주", description = "발주 관리 API")
@RestController
@RequestMapping("/api/purchase-orders")
@SecurityRequirement(name = "bearerAuth")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @Operation(summary = "발주 단건 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "발주 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{purchaseOrderId}")
    public ResponseEntity<PurchaseOrderResponse> get(
            @Parameter(description = "발주 ID", example = "1") @PathVariable Long purchaseOrderId) {
        PurchaseOrder po = purchaseOrderService.get(purchaseOrderId);
        return ResponseEntity.ok(PurchaseOrderResponse.from(po));
    }

    @Operation(summary = "발주 목록 조회/검색")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = PurchaseOrderListResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PurchaseOrderListResponse> list(
            @Parameter(description = "매장 ID (선택)") @RequestParam(required = false) Long storeId,
            @Parameter(description = "매장 통합 검색어 (선택)") @RequestParam(required = false) String storeKeyword,
            @Parameter(description = "매장명 (선택)") @RequestParam(required = false) String storeName,
            @Parameter(description = "매장코드 (선택)") @RequestParam(required = false) String storeCode,
            @Parameter(description = "창고 ID (선택)") @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "공급업체 ID (선택)") @RequestParam(required = false) Long supplierId,
            @Parameter(description = "공급업체명 (선택)") @RequestParam(required = false) String supplierName,
            @Parameter(description = "상태 (선택): DRAFT/ORDERED/RECEIVED/CANCELLED") @RequestParam(required = false) String status,
            @Parameter(description = "생성일 시작 (yyyy-MM-dd)") @RequestParam(required = false) String createdFrom,
            @Parameter(description = "생성일 종료 (yyyy-MM-dd)") @RequestParam(required = false) String createdTo,
            @Parameter(description = "발주일 시작 (yyyy-MM-dd)") @RequestParam(required = false) String orderedFrom,
            @Parameter(description = "발주일 종료 (yyyy-MM-dd)") @RequestParam(required = false) String orderedTo,
            @Parameter(description = "조회 시작일 (yyyy-MM-dd)") @RequestParam(required = false) String from,
            @Parameter(description = "조회 종료일 (yyyy-MM-dd)") @RequestParam(required = false) String to,
            @Parameter(description = "페이지 (0부터)", example = "0") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        return ResponseEntity.ok(
                purchaseOrderService.list(
                        storeId, storeKeyword, storeName, storeCode,
                        warehouseId, supplierId, supplierName, status,
                        createdFrom, createdTo, orderedFrom, orderedTo, from, to, page, size
                )
        );
    }

    @Operation(summary = "발주 확정 (ORDERED)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "확정 성공",
                content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "발주 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{purchaseOrderId}/order")
    public ResponseEntity<PurchaseOrderResponse> order(
            @Parameter(description = "발주 ID", example = "1") @PathVariable Long purchaseOrderId) {
        PurchaseOrder po = purchaseOrderService.order(purchaseOrderId);
        return ResponseEntity.ok(PurchaseOrderResponse.from(po));
    }

    @Operation(summary = "임시저장 발주 내용 수정")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공",
                content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "발주 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{purchaseOrderId}/draft")
    public ResponseEntity<PurchaseOrderResponse> updateDraft(
            @Parameter(description = "발주 ID", example = "1") @PathVariable Long purchaseOrderId,
            @Valid @RequestBody PurchaseOrderDraftUpdateRequest request
    ) {
        PurchaseOrder po = purchaseOrderService.updateDraft(purchaseOrderId, request);
        return ResponseEntity.ok(PurchaseOrderResponse.from(po));
    }

    @Operation(summary = "임시저장 발주 삭제")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "발주 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{purchaseOrderId}/draft")
    public ResponseEntity<Void> deleteDraft(
            @Parameter(description = "발주 ID", example = "1") @PathVariable Long purchaseOrderId) {
        purchaseOrderService.deleteDraft(purchaseOrderId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "발주서 엑셀 다운로드")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "다운로드 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "발주 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{purchaseOrderId}/document")
    public ResponseEntity<byte[]> downloadDocument(
            @Parameter(description = "발주 ID", example = "1") @PathVariable Long purchaseOrderId) {
        byte[] file = purchaseOrderService.exportDocument(purchaseOrderId);
        String filename = purchaseOrderService.buildDocumentDownloadFilename(purchaseOrderId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(file);
    }

    @Operation(summary = "발주 취소 (CANCELLED)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "취소 성공",
                content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "발주 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{purchaseOrderId}/cancel")
    public ResponseEntity<PurchaseOrderResponse> cancel(
            @Parameter(description = "발주 ID", example = "1") @PathVariable Long purchaseOrderId) {
        PurchaseOrder po = purchaseOrderService.cancel(purchaseOrderId);
        return ResponseEntity.ok(PurchaseOrderResponse.from(po));
    }
}