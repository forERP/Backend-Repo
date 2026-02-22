package com.forerp.erp.store.controller;

import com.forerp.erp.common.exception.ApiErrorResponse;
import com.forerp.erp.store.dto.StoreDto;
import com.forerp.erp.store.service.StoreService;
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

@Tag(name = "매장", description = "매장 관리 API")
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StoreController {

    private final StoreService storeService;

    @Operation(summary = "매장 생성")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "생성 성공",
                content = @Content(schema = @Schema(implementation = StoreDto.Response.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<StoreDto.Response> createStore(@RequestBody @Valid StoreDto.CreateRequest request) {
        return ResponseEntity.ok(storeService.createStore(request));
    }

    @Operation(summary = "전체 매장 목록 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = StoreDto.Response.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<StoreDto.Response>> getAllStores() {
        return ResponseEntity.ok(storeService.getAllStores());
    }

    @Operation(summary = "매장 검색 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = StoreDto.Response.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<Page<StoreDto.Response>> searchStores(
            @Parameter(description = "통합 검색어 (선택)") @RequestParam(required = false) String keyword,
            @Parameter(description = "매장명 (선택)") @RequestParam(required = false) String name,
            @Parameter(description = "매장코드 (선택)") @RequestParam(required = false) String code,
            @Parameter(description = "상태 (선택): OPEN/INACTIVE/CLOSED") @RequestParam(required = false) String status,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(storeService.searchStores(keyword, name, code, status, pageable));
    }

    @Operation(summary = "매장 단건 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = StoreDto.Response.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "매장 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<StoreDto.Response> getStore(
            @Parameter(description = "매장 ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(storeService.getStore(id));
    }

    @Operation(summary = "매장 상태 변경")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "변경 성공",
                content = @Content(schema = @Schema(implementation = StoreDto.Response.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "매장 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<StoreDto.Response> updateStatus(
            @Parameter(description = "매장 ID", example = "1") @PathVariable Long id,
            @RequestBody StoreDto.UpdateStatusRequest request) {
        return ResponseEntity.ok(storeService.updateStoreStatus(id, request));
    }

    @Operation(summary = "매장 정보 수정")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공",
                content = @Content(schema = @Schema(implementation = StoreDto.Response.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "매장 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<StoreDto.Response> updateStore(
            @Parameter(description = "매장 ID", example = "1") @PathVariable Long id,
            @RequestBody @Valid StoreDto.UpdateRequest request) {
        return ResponseEntity.ok(storeService.updateStore(id, request));
    }
}