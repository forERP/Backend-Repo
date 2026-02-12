package com.forerp.erp.warehouse.controller;

import com.forerp.erp.warehouse.domain.Warehouse;
import com.forerp.erp.warehouse.dto.WarehouseResponseDto;
import com.forerp.erp.warehouse.dto.WarehouseRequestDto;
import com.forerp.erp.warehouse.dto.WarehouseUpdateRequestDto;
import com.forerp.erp.warehouse.repository.WarehouseRepository;
import com.forerp.erp.store.repository.StoreRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
        private final StoreRepository storeRepository;

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

        @Operation(summary = "창고 생성")
        @PostMapping
        public ResponseEntity<WarehouseResponseDto> createWarehouse(@RequestBody @Valid WarehouseRequestDto request) {
                var store = storeRepository.findById(request.getStoreId())
                                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다: " + request.getStoreId()));

                Warehouse warehouse = Warehouse.create(store, request.getCode(), request.getName());
                Warehouse saved = warehouseRepository.save(warehouse);
                return ResponseEntity.ok(WarehouseResponseDto.from(saved));
        }

        @Operation(summary = "창고 수정 (이름/코드/활성화)")
        @PutMapping("/{warehouseId}")
        public ResponseEntity<WarehouseResponseDto> updateWarehouse(@PathVariable Long warehouseId,
                                                                                                                                 @RequestBody WarehouseUpdateRequestDto request) {
                Warehouse warehouse = warehouseRepository.findById(warehouseId)
                                .orElseThrow(() -> new IllegalArgumentException("창고를 찾을 수 없습니다: " + warehouseId));

                warehouse.updateInfo(request.getCode(), request.getName(), request.getActive());
                Warehouse saved = warehouseRepository.save(warehouse);
                return ResponseEntity.ok(WarehouseResponseDto.from(saved));
        }
}
