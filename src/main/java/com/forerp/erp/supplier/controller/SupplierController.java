package com.forerp.erp.supplier.controller;

import com.forerp.erp.supplier.dto.SupplierCreateRequestDto;
import com.forerp.erp.supplier.dto.SupplierResponseDto;
import com.forerp.erp.supplier.dto.SupplierUpdateRequestDto;
import com.forerp.erp.supplier.service.SupplierService;
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

@Tag(name = "Supplier", description = "거래처 관리 API (관리자 전용)")
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "거래처 단건 조회")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = SupplierResponseDto.class)))
    @GetMapping("/{supplierId}")
    public ResponseEntity<SupplierResponseDto> getSupplier(@PathVariable Long supplierId) {
        return ResponseEntity.ok(supplierService.getSupplier(supplierId));
    }

    @Operation(summary = "거래처 목록 조회(전체)")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = SupplierResponseDto.class)))
    @GetMapping
    public ResponseEntity<List<SupplierResponseDto>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @Operation(summary = "거래처 검색 조회")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = SupplierResponseDto.class)))
    @GetMapping("/search")
    public ResponseEntity<Page<SupplierResponseDto>> searchSuppliers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String contactName,
            @RequestParam(required = false) String status,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(supplierService.searchSuppliers(name, contactName, status, pageable));
    }

    @Operation(summary = "거래처 생성")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = SupplierResponseDto.class)))
    @PostMapping
    public ResponseEntity<SupplierResponseDto> createSupplier(@Valid @RequestBody SupplierCreateRequestDto request) {
        return ResponseEntity.ok(supplierService.createSupplier(request));
    }

    @Operation(summary = "거래처 수정")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = SupplierResponseDto.class)))
    @PutMapping("/{supplierId}")
    public ResponseEntity<SupplierResponseDto> updateSupplier(
            @PathVariable Long supplierId,
            @Valid @RequestBody SupplierUpdateRequestDto request
    ) {
        return ResponseEntity.ok(supplierService.updateSupplier(supplierId, request));
    }
}
