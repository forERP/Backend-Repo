package com.forerp.erp.supplier.controller;

import com.forerp.erp.supplier.domain.Supplier;
import com.forerp.erp.supplier.dto.SupplierResponseDto;
import com.forerp.erp.supplier.repository.SupplierRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Supplier", description = "거래처 관리 API (관리자 전용)")
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierRepository supplierRepository;

    @Operation(summary = "거래처 단건 조회")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = SupplierResponseDto.class)))
    @GetMapping("/{supplierId}")
    public ResponseEntity<SupplierResponseDto> getSupplier(@PathVariable Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("거래처를 찾을 수 없습니다: " + supplierId));
        return ResponseEntity.ok(SupplierResponseDto.from(supplier));
    }

    @Operation(summary = "거래처 목록 조회")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = SupplierResponseDto.class)))
    @GetMapping
    public ResponseEntity<List<SupplierResponseDto>> getAllSuppliers() {
        List<SupplierResponseDto> suppliers = supplierRepository.findAll().stream()
                .map(SupplierResponseDto::from)
                .toList();
        return ResponseEntity.ok(suppliers);
    }
}
