package com.forerp.erp.warehouse.controller;

import com.forerp.erp.warehouse.domain.Warehouse;
import com.forerp.erp.warehouse.dto.WarehouseResponseDto;
import com.forerp.erp.warehouse.dto.WarehouseRequestDto;
import com.forerp.erp.warehouse.dto.WarehouseUpdateRequestDto;
import com.forerp.erp.warehouse.repository.WarehouseRepository;
import com.forerp.erp.store.repository.StoreRepository;
import com.forerp.erp.storeproduct.service.StoreProductSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    private final StoreProductSyncService storeProductSyncService;

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

    @GetMapping("/search")
    public ResponseEntity<Page<WarehouseResponseDto>> searchWarehouses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String status,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<WarehouseResponseDto> page = warehouseRepository.search(
                        normalize(keyword),
                        normalize(name),
                        normalize(code),
                        parseActive(status),
                        pageable)
                .map(WarehouseResponseDto::from);
        return ResponseEntity.ok(page);
    }

        @Operation(summary = "창고 생성")
        @PostMapping
        public ResponseEntity<WarehouseResponseDto> createWarehouse(@RequestBody @Valid WarehouseRequestDto request) {
                var store = storeRepository.findById(request.getStoreId())
                                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다: " + request.getStoreId()));

                Warehouse warehouse = Warehouse.create(
                        store,
                        request.getCode(),
                        request.getName(),
                        request.getAddress(),
                        request.getLatitude(),
                        request.getLongitude()
                );
                Warehouse saved = warehouseRepository.save(warehouse);
                storeProductSyncService.syncActiveProductsToWarehouse(saved);
                return ResponseEntity.ok(WarehouseResponseDto.from(saved));
        }

        @Operation(summary = "창고 수정 (이름/코드/활성화)")
        @PutMapping("/{warehouseId}")
        public ResponseEntity<WarehouseResponseDto> updateWarehouse(@PathVariable Long warehouseId,
                                                                                                                                 @RequestBody WarehouseUpdateRequestDto request) {
                Warehouse warehouse = warehouseRepository.findById(warehouseId)
                                .orElseThrow(() -> new IllegalArgumentException("창고를 찾을 수 없습니다: " + warehouseId));

                warehouse.updateInfo(
                        request.getCode(),
                        request.getName(),
                        request.getAddress(),
                        request.getLatitude(),
                        request.getLongitude(),
                        request.getActive()
                );
                Warehouse saved = warehouseRepository.save(warehouse);
                return ResponseEntity.ok(WarehouseResponseDto.from(saved));
        }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Boolean parseActive(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case "ACTIVE" -> true;
            case "INACTIVE" -> false;
            default -> throw new IllegalArgumentException("Invalid warehouse status: " + status);
        };
    }
}
