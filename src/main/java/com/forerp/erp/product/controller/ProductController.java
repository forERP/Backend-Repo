package com.forerp.erp.product.controller;

import com.forerp.erp.product.dto.*;
import com.forerp.erp.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@Tag(name = "Product", description = "상품 관리 API (관리자)")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 생성", description = "신규 상품을 등록")
    @ApiResponse(responseCode = "201", description = "Created",
            content = @Content(schema = @Schema(implementation = ProductCreateResponseDto.class)))
    @PostMapping
    public ResponseEntity<ProductCreateResponseDto> createProduct(
            @Valid @RequestBody ProductCreateRequestDto request
    ) {
        return ResponseEntity.status(201).body(productService.createProduct(request));
    }

    @Operation(
            summary = "상품 목록 조회 (페이지네이션)",
            description = "id DESC, page=0, size=20"
    )
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = Page.class)))
    @GetMapping
    public ResponseEntity<Page<ProductListResponseDto>> getProducts(
            @RequestParam(required = false) String productKeyword,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String status,
            @Parameter(description = "페이지 정보(page/size)")
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ){
        return ResponseEntity.ok(productService.getAllProducts(productKeyword, name, sku, status, pageable));
    }

    @Operation(summary = "상품 단건 조회", description = "상품 ID로 단건 조회")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = ProductDto.DetailResponse.class)))
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto.DetailResponse> getProduct(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long id
    ){
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @Operation(summary = "상품 수정", description = "상품 정보 수정")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = ProductDto.DetailResponse.class)))
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto.DetailResponse> updateProduct(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ProductDto.UpdateRequest request
    ){
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @Operation(summary = "상품 단종 처리", description = "상품을 단종(DISCONTINUED) 상태로 변경")
    @ApiResponse(responseCode = "204", description = "No Content")
    @PatchMapping("/{id}/discontinue")
    public ResponseEntity<Void> discontinueProduct(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long id
    ){
        productService.discontinueProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "상품 재등록(단종 취소)", description = "단종 상품을 다시 활성(ACTIVE) 상태로 변경")
    @ApiResponse(responseCode = "204", description = "No Content")
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivateProduct(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long id
    ){
        productService.reactivateProduct(id);
        return ResponseEntity.noContent().build();
    }
}
