package com.forerp.erp.product.controller;

import com.forerp.erp.common.exception.ApiErrorResponse;
import com.forerp.erp.product.dto.ProductCategoryCreateRequestDto;
import com.forerp.erp.product.dto.ProductCategoryCreateResponseDto;
import com.forerp.erp.product.dto.ProductCategoryDetailResponseDto;
import com.forerp.erp.product.dto.ProductCategoryResponseDto;
import com.forerp.erp.product.dto.ProductCategoryUpdateRequestDto;
import com.forerp.erp.product.service.ProductCategoryService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "상품 카테고리", description = "상품 카테고리 등록·조회·수정 API (백오피스)")
@RestController
@RequestMapping("/api/product-categories")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    @Operation(summary = "전체 카테고리 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<List<ProductCategoryResponseDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "카테고리 검색 (페이지네이션)", description = "코드·이름·상태 조건으로 카테고리를 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/search")
    public ResponseEntity<Page<ProductCategoryResponseDto>> searchCategories(
            @Parameter(description = "통합 키워드 (코드/이름)") @RequestParam(required = false) String keyword,
            @Parameter(description = "카테고리 이름") @RequestParam(required = false) String name,
            @Parameter(description = "카테고리 코드") @RequestParam(required = false) String code,
            @Parameter(description = "활성 상태 (active / inactive)") @RequestParam(required = false) String status,
            @Parameter(description = "페이지 번호 (0부터)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(categoryService.searchCategories(keyword, name, code, status, page, size));
    }

    @Operation(summary = "카테고리 단건 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ProductCategoryDetailResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "카테고리 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<ProductCategoryDetailResponseDto> getCategory(
            @Parameter(description = "카테고리 ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategory(id));
    }

    @Operation(summary = "카테고리 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = ProductCategoryCreateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 오류",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "코드 중복",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductCategoryCreateResponseDto createCategory(
            @RequestBody @Valid ProductCategoryCreateRequestDto request) {
        return categoryService.createCategory(request);
    }

    @Operation(summary = "카테고리 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ProductCategoryDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 오류",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "카테고리 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<ProductCategoryDetailResponseDto> updateCategory(
            @Parameter(description = "카테고리 ID", example = "1") @PathVariable Long id,
            @RequestBody @Valid ProductCategoryUpdateRequestDto request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }
}