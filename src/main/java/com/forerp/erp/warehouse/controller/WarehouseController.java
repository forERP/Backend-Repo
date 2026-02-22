package com.forerp.erp.warehouse.controller;

import com.forerp.erp.auditlog.AuditLogAction;
import com.forerp.erp.auditlog.AuditLogService;
import com.forerp.erp.auditlog.AuditLogTargetType;
import com.forerp.erp.common.exception.ApiErrorResponse;
import com.forerp.erp.warehouse.domain.Warehouse;
import com.forerp.erp.warehouse.dto.WarehouseResponseDto;
import com.forerp.erp.warehouse.dto.WarehouseRequestDto;
import com.forerp.erp.warehouse.dto.WarehouseUpdateRequestDto;
import com.forerp.erp.warehouse.repository.WarehouseRepository;
import com.forerp.erp.store.repository.StoreRepository;
import com.forerp.erp.storeproduct.service.StoreProductSyncService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "창고", description = "창고 관리 API")
@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class WarehouseController {

    private final WarehouseRepository warehouseRepository;
    private final StoreRepository storeRepository;
    private final StoreProductSyncService storeProductSyncService;
    private final AuditLogService auditLogService;

    @Operation(summary = "창고 단건 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = WarehouseResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "창고 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{warehouseId}")
    public ResponseEntity<WarehouseResponseDto> getWarehouse(
            @Parameter(description = "창고 ID", example = "1") @PathVariable Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("창고를 찾을 수 없습니다: " + warehouseId));
        return ResponseEntity.ok(WarehouseResponseDto.from(warehouse));
    }

    @Operation(summary = "창고 목록 조회 (전체 또는 매장별)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = WarehouseResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<WarehouseResponseDto>> getWarehouses(
            @Parameter(description = "매장 ID (선택)", example = "1") @RequestParam(required = false) Long storeId
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

    @Operation(summary = "창고 검색 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = WarehouseResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<Page<WarehouseResponseDto>> searchWarehouses(
            @Parameter(description = "통합 검색어 (선택)") @RequestParam(required = false) String keyword,
            @Parameter(description = "창고명 (선택)") @RequestParam(required = false) String name,
            @Parameter(description = "창고코드 (선택)") @RequestParam(required = false) String code,
            @Parameter(description = "상태 (선택): ACTIVE/INACTIVE") @RequestParam(required = false) String status,
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
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "생성 성공",
                content = @Content(schema = @Schema(implementation = WarehouseResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
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
        auditLogService.logCurrentUserAction(
                AuditLogAction.WAREHOUSE_CREATE,
                AuditLogTargetType.WAREHOUSE,
                saved.getId()
        );
        return ResponseEntity.ok(WarehouseResponseDto.from(saved));
    }

    @Operation(summary = "창고 수정 (이름/주소/활성여부)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공",
                content = @Content(schema = @Schema(implementation = WarehouseResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "창고 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{warehouseId}")
    public ResponseEntity<WarehouseResponseDto> updateWarehouse(
            @Parameter(description = "창고 ID", example = "1") @PathVariable Long warehouseId,
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
        auditLogService.logCurrentUserAction(
                AuditLogAction.WAREHOUSE_UPDATE,
                AuditLogTargetType.WAREHOUSE,
                saved.getId()
        );
        return ResponseEntity.ok(WarehouseResponseDto.from(saved));
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Boolean parseActive(String status) {
        if (status == null || status.isBlank()) return null;
        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case "ACTIVE" -> true;
            case "INACTIVE" -> false;
            default -> throw new IllegalArgumentException("Invalid warehouse status: " + status);
        };
    }
}