package com.forerp.erp.inventory.controller;

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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "Inventory Admin", description = "Inventory query and store product sale control APIs")
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryAdminController {

    private final InventoryQueryService inventoryQueryService;
    private final InventoryCommandService inventoryCommandService;

    @Operation(summary = "List inventory with store/warehouse/product/status filters")
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(schema = @Schema(implementation = InventoryListResponse.class))
    )
    @GetMapping
    public ResponseEntity<InventoryListResponse> listInventory(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String storeKeyword,
            @RequestParam(required = false) String warehouseKeyword,
            @RequestParam(required = false) String productKeyword,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) SaleStatus saleStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String resolvedProductKeyword = (productKeyword == null || productKeyword.isBlank())
                ? keyword
                : productKeyword;

        return ResponseEntity.ok(inventoryQueryService.list(
                storeId,
                warehouseId,
                storeKeyword,
                warehouseKeyword,
                resolvedProductKeyword,
                saleStatus,
                page,
                size
        ));
    }

    @Operation(summary = "Get inventory detail by storeProductId")
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(schema = @Schema(implementation = InventoryResponse.class))
    )
    @GetMapping("/{storeProductId}")
    public ResponseEntity<InventoryResponse> getInventoryDetail(
            @PathVariable Long storeProductId
    ) {
        return ResponseEntity.ok(inventoryQueryService.getByStoreProductId(storeProductId));
    }

    @Operation(summary = "Update inventory detail (saleStatus, salePrice)")
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(schema = @Schema(implementation = InventoryResponse.class))
    )
    @PatchMapping("/{storeProductId}")
    public ResponseEntity<InventoryResponse> updateInventory(
            @PathVariable Long storeProductId,
            @RequestBody InventoryUpdateRequest request
    ) {
        return ResponseEntity.ok(inventoryCommandService.update(storeProductId, request));
    }

    @Operation(summary = "Update sale status by storeProductId")
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(schema = @Schema(implementation = InventoryResponse.class))
    )
    @PatchMapping("/{storeProductId}/sale-status")
    public ResponseEntity<InventoryResponse> updateSaleStatus(
            @PathVariable Long storeProductId,
            @Valid @RequestBody InventorySaleStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(inventoryCommandService.updateSaleStatus(storeProductId, request.getSaleStatus()));
    }

    @Operation(summary = "Adjust inventory quantity by store/warehouse/product")
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(schema = @Schema(implementation = InventoryAdjustResponse.class))
    )
    @PatchMapping("/adjustments")
    public ResponseEntity<InventoryAdjustResponse> adjustInventory(
            @AuthenticationPrincipal User actor,
            @Valid @RequestBody InventoryAdjustRequest request
    ) {
        return ResponseEntity.ok(inventoryCommandService.adjust(actor, request));
    }
}
