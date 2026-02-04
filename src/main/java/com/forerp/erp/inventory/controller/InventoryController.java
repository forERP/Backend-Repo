package com.forerp.erp.inventory.controller;

import com.forerp.erp.inventory.dto.InventoryListResponse;
import com.forerp.erp.inventory.dto.InventoryResponse;
import com.forerp.erp.inventory.service.InventoryQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Inventory", description = "매장 재고 조회 API")
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryQueryService inventoryQueryService;

    @Operation(summary = "매장 재고 목록 조회")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = InventoryListResponse.class)))
    @GetMapping("/{storeId}/inventory")
    public ResponseEntity<InventoryListResponse> listInventory(
            @PathVariable Long storeId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(inventoryQueryService.list(storeId, warehouseId, keyword, page, size));
    }

    @Operation(summary = "매장 단일 상품 재고 조회")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = InventoryResponse.class)))
    @GetMapping("/{storeId}/inventory/{productId}")
    public ResponseEntity<InventoryResponse> getInventory(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @RequestParam(required = false) Long warehouseId
    ) {
        return ResponseEntity.ok(inventoryQueryService.get(storeId, warehouseId, productId));
    }
}