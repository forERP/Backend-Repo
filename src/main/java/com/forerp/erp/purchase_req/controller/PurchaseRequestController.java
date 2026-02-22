package com.forerp.erp.purchase_req.controller;

import com.forerp.erp.common.exception.ApiErrorResponse;
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "구매요청", description = "구매요청 관리 API")
@RestController
@RequestMapping("/api/purchase-requests")
@SecurityRequirement(name = "bearerAuth")
public class PurchaseRequestController {

    private final PurchaseRequestService purchaseRequestService;

    public PurchaseRequestController(PurchaseRequestService purchaseRequestService) {
        this.purchaseRequestService = purchaseRequestService;
    }

    @Operation(summary = "구매요청 생성")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 성공",
                content = @Content(schema = @Schema(implementation = PurchaseRequestResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<PurchaseRequestResponse> create(
            @Valid @RequestBody PurchaseRequestCreateRequest request,
            @AuthenticationPrincipal User actor
    ) {
        PurchaseRequest pr = purchaseRequestService.create(request, actor);
        return ResponseEntity.status(201).body(PurchaseRequestResponse.from(pr));
    }

    @Operation(summary = "구매요청 단건 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = PurchaseRequestResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "구매요청 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{purchaseRequestId}")
    public ResponseEntity<PurchaseRequestResponse> get(
            @Parameter(description = "구매요청 ID", example = "1") @PathVariable Long purchaseRequestId) {
        PurchaseRequest pr = purchaseRequestService.get(purchaseRequestId);
        return ResponseEntity.ok(PurchaseRequestResponse.from(pr));
    }

    @Operation(summary = "구매요청 목록 조회/검색")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = PurchaseRequestListResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PurchaseRequestListResponse> list(
            @Parameter(description = "매장 ID (선택)") @RequestParam(required = false) Long storeId,
            @Parameter(description = "매장 통합 검색어 (선택)") @RequestParam(required = false) String storeKeyword,
            @Parameter(description = "매장명 (선택)") @RequestParam(required = false) String storeName,
            @Parameter(description = "매장코드 (선택)") @RequestParam(required = false) String storeCode,
            @Parameter(description = "상태 (선택): PENDING/APPROVED/REJECTED") @RequestParam(required = false) String status,
            @Parameter(description = "조회 시작일 (yyyy-MM-dd)") @RequestParam(required = false) String from,
            @Parameter(description = "조회 종료일 (yyyy-MM-dd)") @RequestParam(required = false) String to,
            @Parameter(description = "페이지 (0부터)", example = "0") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        return ResponseEntity.ok(purchaseRequestService.list(
                storeId, storeKeyword, storeName, storeCode, status, from, to, page, size));
    }

    @Operation(summary = "구매요청 임시발주 생성")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "생성 성공",
                content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "구매요청 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{purchaseRequestId}/draft-order")
    public ResponseEntity<PurchaseOrderResponse> createDraftOrder(
            @Parameter(description = "구매요청 ID", example = "1") @PathVariable Long purchaseRequestId,
            @Valid @RequestBody PurchaseRequestApproveRequest request,
            @AuthenticationPrincipal User actor
    ) {
        PurchaseOrder po = purchaseRequestService.createDraftOrder(purchaseRequestId, request, actor);
        return ResponseEntity.ok(PurchaseOrderResponse.from(po));
    }

    @Operation(summary = "구매요청 임시발주 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class))),
        @ApiResponse(responseCode = "404", description = "임시발주 없음"),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{purchaseRequestId}/draft-order")
    public ResponseEntity<PurchaseOrderResponse> getDraftOrder(
            @Parameter(description = "구매요청 ID", example = "1") @PathVariable Long purchaseRequestId) {
        return purchaseRequestService.getDraftOrder(purchaseRequestId)
                .map(po -> ResponseEntity.ok(PurchaseOrderResponse.from(po)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "구매요청 승인 (발주 확정)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "승인 성공",
                content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "구매요청 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{purchaseRequestId}/approve")
    public ResponseEntity<PurchaseOrderResponse> approve(
            @Parameter(description = "구매요청 ID", example = "1") @PathVariable Long purchaseRequestId,
            @AuthenticationPrincipal User actor
    ) {
        PurchaseOrder po = purchaseRequestService.approve(purchaseRequestId, actor);
        return ResponseEntity.ok(PurchaseOrderResponse.from(po));
    }

    @Operation(summary = "구매요청 반려")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "반려 성공",
                content = @Content(schema = @Schema(implementation = PurchaseRequestResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "구매요청 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{purchaseRequestId}/reject")
    public ResponseEntity<PurchaseRequestResponse> reject(
            @Parameter(description = "구매요청 ID", example = "1") @PathVariable Long purchaseRequestId,
            @AuthenticationPrincipal User actor
    ) {
        PurchaseRequest pr = purchaseRequestService.reject(purchaseRequestId, actor);
        return ResponseEntity.ok(PurchaseRequestResponse.from(pr));
    }
}