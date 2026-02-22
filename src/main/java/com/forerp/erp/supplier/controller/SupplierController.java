package com.forerp.erp.supplier.controller;

import com.forerp.erp.common.exception.ApiErrorResponse;
import com.forerp.erp.supplier.dto.SupplierCreateRequestDto;
import com.forerp.erp.supplier.dto.SupplierResponseDto;
import com.forerp.erp.supplier.dto.SupplierUpdateRequestDto;
import com.forerp.erp.supplier.service.SupplierService;
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

@Tag(name = "공급업체", description = "공급업체 관리 API")
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "공급업체 단건 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = SupplierResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "공급업체 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{supplierId}")
    public ResponseEntity<SupplierResponseDto> getSupplier(
            @Parameter(description = "공급업체 ID", example = "1") @PathVariable Long supplierId) {
        return ResponseEntity.ok(supplierService.getSupplier(supplierId));
    }

    @Operation(summary = "공급업체 목록 조회 (전체)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = SupplierResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<SupplierResponseDto>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @Operation(summary = "공급업체 검색 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = SupplierResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<Page<SupplierResponseDto>> searchSuppliers(
            @Parameter(description = "공급업체명 (선택)") @RequestParam(required = false) String name,
            @Parameter(description = "담당자명 (선택)") @RequestParam(required = false) String contactName,
            @Parameter(description = "상태 (선택): ACTIVE/INACTIVE") @RequestParam(required = false) String status,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(supplierService.searchSuppliers(name, contactName, status, pageable));
    }

    @Operation(summary = "공급업체 생성")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "생성 성공",
                content = @Content(schema = @Schema(implementation = SupplierResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<SupplierResponseDto> createSupplier(@Valid @RequestBody SupplierCreateRequestDto request) {
        return ResponseEntity.ok(supplierService.createSupplier(request));
    }

    @Operation(summary = "공급업체 수정")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공",
                content = @Content(schema = @Schema(implementation = SupplierResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "공급업체 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{supplierId}")
    public ResponseEntity<SupplierResponseDto> updateSupplier(
            @Parameter(description = "공급업체 ID", example = "1") @PathVariable Long supplierId,
            @Valid @RequestBody SupplierUpdateRequestDto request
    ) {
        return ResponseEntity.ok(supplierService.updateSupplier(supplierId, request));
    }
}