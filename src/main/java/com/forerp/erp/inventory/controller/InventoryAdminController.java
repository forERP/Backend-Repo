package com.forerp.erp.inventory.controller;

import com.forerp.erp.common.exception.ApiErrorResponse;
import com.forerp.erp.inventory.dto.InventoryAdjustRequest;
import com.forerp.erp.inventory.dto.InventoryAdjustResponse;
import com.forerp.erp.inventory.dto.InventoryListResponse;
import com.forerp.erp.inventory.dto.InventoryResponse;
import com.forerp.erp.inventory.dto.InventorySaleStatusUpdateRequest;
import com.forerp.erp.inventory.dto.InventoryUpdateRequest;
import com.forerp.erp.inventory.service.InventoryCommandService;
import com.forerp.erp.inventory.service.InventoryQueryService;
import com.forerp.erp.storeproduct.domain.SaleStatus;
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

@Tag(name = "재고 (관리)", description = "재고 조회·수정·판매상태·수동조정 관리 API (백오피스)")
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryAdminController {

    private final InventoryQueryService inventoryQueryService;
    private final InventoryCommandService inventoryCommandService;

    @Operation(summary = "재고 목록 조회 / 검색", description = "매장·창고·상품·판매상태 조건으로 재고 목록을 페이지 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = InventoryListResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<InventoryListResponse> listInventory(
            @Parameter(description = "매장 ID") @RequestParam(required = false) Long storeId,
            @Parameter(description = "창고 ID") @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "매장 통합 키워드") @RequestParam(required = false) String storeKeyword,
            @Parameter(description = "창고 키워드") @RequestParam(required = false) String warehouseKeyword,
            @Parameter(description = "상품 키워드") @RequestParam(required = false) String productKeyword,
            @Parameter(description = "통합 키워드 (productKeyword 없을 때 대체)") @RequestParam(required = false) String keyword,
            @Parameter(description = "판매 상태 (ON / OFF)") @RequestParam(required = false) SaleStatus saleStatus,
            @Parameter(description = "페이지 번호 (0부터)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        String resolvedProductKeyword = (productKeyword == null || productKeyword.isBlank())
                ? keyword
                : productKeyword;
        return ResponseEntity.ok(inventoryQueryService.list(
                storeId, warehouseId, storeKeyword, warehouseKeyword,
                resolvedProductKeyword, saleStatus, page, size));
    }

    @Operation(summary = "재고 상세 조회", description = "매장 상품 ID(storeProductId)로 재고 상세 정보를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = InventoryResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "재고 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{storeProductId}")
    public ResponseEntity<InventoryResponse> getInventoryDetail(
            @Parameter(description = "매장 상품 ID", example = "1") @PathVariable Long storeProductId
    ) {
        return ResponseEntity.ok(inventoryQueryService.getByStoreProductId(storeProductId));
    }

    @Operation(summary = "재고 정보 수정", description = "판매상태(saleStatus)·매장 판매가(salePrice)를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = InventoryResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "재고 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{storeProductId}")
    public ResponseEntity<InventoryResponse> updateInventory(
            @Parameter(description = "매장 상품 ID", example = "1") @PathVariable Long storeProductId,
            @RequestBody InventoryUpdateRequest request
    ) {
        return ResponseEntity.ok(inventoryCommandService.update(storeProductId, request));
    }

    @Operation(summary = "판매 상태 변경", description = "매장 상품의 판매 상태를 ON / OFF 로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공",
                    content = @Content(schema = @Schema(implementation = InventoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력값 오류",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "재고 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{storeProductId}/sale-status")
    public ResponseEntity<InventoryResponse> updateSaleStatus(
            @Parameter(description = "매장 상품 ID", example = "1") @PathVariable Long storeProductId,
            @Valid @RequestBody InventorySaleStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(inventoryCommandService.updateSaleStatus(storeProductId, request.getSaleStatus()));
    }

    @Operation(summary = "재고 수동 조정", description = "매장·창고·상품 ID로 재고 수량을 직접 증감합니다. IN=입고, OUT=출고.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조정 성공",
                    content = @Content(schema = @Schema(implementation = InventoryAdjustResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력값 오류",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/adjustments")
    public ResponseEntity<InventoryAdjustResponse> adjustInventory(
            @AuthenticationPrincipal User actor,
            @Valid @RequestBody InventoryAdjustRequest request
    ) {
        return ResponseEntity.ok(inventoryCommandService.adjust(actor, request));
    }
}