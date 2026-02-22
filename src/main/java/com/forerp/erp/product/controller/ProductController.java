package com.forerp.erp.product.controller;

import com.forerp.erp.common.exception.ApiErrorResponse;
import com.forerp.erp.product.dto.*;
import com.forerp.erp.product.service.ProductService;
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

@Tag(name = "상품", description = "상품 등록·조회·수정·단종 관리 API (백오피스)")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 생성", description = "단일 상품을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "상품 생성 성공",
                    content = @Content(schema = @Schema(implementation = ProductCreateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 오류",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ProductCreateResponseDto> createProduct(
            @Valid @RequestBody ProductCreateRequestDto request) {
        return ResponseEntity.status(201).body(productService.createProduct(request));
    }

    @Operation(summary = "묶음 상품 생성", description = "활성 상품들을 구성해 세트(SET) 묶음 상품을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "묶음 상품 생성 성공",
                    content = @Content(schema = @Schema(implementation = ProductBundleCreateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 오류",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/bundles")
    public ResponseEntity<ProductBundleCreateResponseDto> createBundleProduct(
            @Valid @RequestBody ProductBundleCreateRequestDto request) {
        return ResponseEntity.status(201).body(productService.createBundleProduct(request));
    }

    @Operation(summary = "묶음 상품 구성 후보 목록", description = "묶음으로 구성 가능한 활성 단일 상품 목록을 반환합니다. (SET 타입 제외)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/bundle-candidates")
    public ResponseEntity<List<ProductBundleCandidateResponseDto>> getBundleCandidates() {
        return ResponseEntity.ok(productService.getBundleCandidates());
    }

    @Operation(summary = "상품 목록 조회 (페이지네이션)", description = "기본 정렬: id DESC, page=0, size=20")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<Page<ProductListResponseDto>> getProducts(
            @Parameter(description = "통합 키워드 (이름/SKU)") @RequestParam(required = false) String productKeyword,
            @Parameter(description = "상품 이름") @RequestParam(required = false) String name,
            @Parameter(description = "SKU") @RequestParam(required = false) String sku,
            @Parameter(description = "상품 상태 (ACTIVE / DISCONTINUED)") @RequestParam(required = false) String status,
            @Parameter(description = "페이지 정보 (page / size)")
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(productKeyword, name, sku, status, pageable));
    }

    @Operation(summary = "상품 단건 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ProductDto.DetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "상품 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto.DetailResponse> getProduct(
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @Operation(summary = "상품 수정", description = "상품 이름·카테고리·가격·설명·이미지를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ProductDto.DetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 오류",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "상품 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto.DetailResponse> updateProduct(
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody ProductDto.UpdateRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @Operation(summary = "상품 단종 처리", description = "상품을 DISCONTINUED 상태로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "단종 처리 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "상품 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id}/discontinue")
    public ResponseEntity<Void> discontinueProduct(
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long id) {
        productService.discontinueProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "상품 재활성화 (단종 취소)", description = "단종된 상품을 다시 ACTIVE 상태로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "재활성화 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "상품 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivateProduct(
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long id) {
        productService.reactivateProduct(id);
        return ResponseEntity.noContent().build();
    }
}