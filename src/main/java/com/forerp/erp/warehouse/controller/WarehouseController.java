package com.forerp.erp.warehouse.controller;

import com.forerp.erp.warehouse.domain.Warehouse;
import com.forerp.erp.warehouse.dto.WarehouseResponseDto;
import com.forerp.erp.warehouse.repository.WarehouseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Warehouse", description = "창고 관리 API (관리자 전용)")
@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseRepository warehouseRepository;

    @Operation(summary = "창고 단건 조회")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = WarehouseResponseDto.class)))
    @GetMapping("/{warehouseId}")
    public ResponseEntity<WarehouseResponseDto> getWarehouse(@PathVariable Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("창고를 찾을 수 없습니다: " + warehouseId));
        return ResponseEntity.ok(WarehouseResponseDto.from(warehouse));
    }

    @Operation(summary = "창고 목록 조회 (전체 또는 지점별)")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = WarehouseResponseDto.class)))
    @GetMapping
    public ResponseEntity<List<WarehouseResponseDto>> getWarehouses(
            @RequestParam(required = false) Long storeId
    ) {
        List<WarehouseResponseDto> warehouses;
        if (storeId != null) {
            warehouses = warehouseRepository.findByStore_Id(storeId).stream()
                    .map(WarehouseResponseDto::from)
                    .toList();
        } else {
            warehouses = warehouseRepository.findAll().stream()
                    .map(WarehouseResponseDto::from)
                    .toList();
        }
        return ResponseEntity.ok(warehouses);
    }
}
