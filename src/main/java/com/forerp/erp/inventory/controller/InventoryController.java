package com.forerp.erp.inventory.controller;

import com.forerp.erp.common.exception.ApiErrorResponse;
import com.forerp.erp.inventory.dto.InventoryListResponse;
import com.forerp.erp.inventory.dto.InventoryResponse;
import com.forerp.erp.inventory.service.InventoryQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "재고 (매장)", description = "매장 단위 재고 조회 API")
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryQueryService inventoryQueryService;

    @Operation(summary = "매장 재고 목록 조회", description = "매장 ID로 재고 목록을 페이지 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = InventoryListResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "매장 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{storeId}/inventory")
    public ResponseEntity<InventoryListResponse> listInventory(
            @Parameter(description = "매장 ID", example = "1") @PathVariable Long storeId,
            @Parameter(description = "창고 ID (선택)") @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "상품명/SKU 키워드") @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(inventoryQueryService.list(storeId, warehouseId, keyword, page, size));
    }

    @Operation(summary = "매장 특정 상품 재고 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = InventoryResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "재고 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{storeId}/inventory/{productId}")
    public ResponseEntity<InventoryResponse> getInventory(
            @Parameter(description = "매장 ID", example = "1") @PathVariable Long storeId,
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long productId,
            @Parameter(description = "창고 ID (선택)") @RequestParam(required = false) Long warehouseId
    ) {
        return ResponseEntity.ok(inventoryQueryService.get(storeId, warehouseId, productId));
    }
}