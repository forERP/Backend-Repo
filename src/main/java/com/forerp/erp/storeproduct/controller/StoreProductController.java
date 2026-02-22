package com.forerp.erp.storeproduct.controller;

import com.forerp.erp.common.exception.ApiErrorResponse;
import com.forerp.erp.store.dto.StoreProductListResponseDto;
import com.forerp.erp.storeproduct.service.StoreProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "매장 상품", description = "매장에 등록된 상품 목록 조회 API")
@RestController
@RequestMapping("/api/store_products")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StoreProductController {

    private final StoreProductService storeProductService;

    @Operation(summary = "매장 상품 목록 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = StoreProductListResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/list")
    public ResponseEntity<Page<StoreProductListResponseDto>> getStoreProductList(
            @Parameter(description = "매장 ID", example = "1", required = true) @RequestParam Long storeId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(storeProductService.getStoreProductList(storeId, pageable));
    }
}