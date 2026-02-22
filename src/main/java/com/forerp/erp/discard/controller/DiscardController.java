package com.forerp.erp.discard.controller;

import com.forerp.erp.common.exception.ApiErrorResponse;
import com.forerp.erp.discard.domain.Discard;
import com.forerp.erp.discard.dto.DiscardCreateRequest;
import com.forerp.erp.discard.dto.DiscardListResponse;
import com.forerp.erp.discard.dto.DiscardResponse;
import com.forerp.erp.discard.service.DiscardService;
import com.forerp.erp.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "폐기", description = "재고 폐기 (창고 재고 감소) API")
@RestController
@RequestMapping("/api/discards")
@SecurityRequirement(name = "bearerAuth")
public class DiscardController {

    private final DiscardService discardService;

    public DiscardController(DiscardService discardService) {
        this.discardService = discardService;
    }

    @Operation(summary = "폐기 생성", description = "폐기 문서를 생성합니다. (창고 재고는 확정 시점에 감소)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 성공",
                content = @Content(schema = @Schema(implementation = DiscardResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<DiscardResponse> create(
            @Valid @RequestBody DiscardCreateRequest request,
            @AuthenticationPrincipal User actor
    ) {
        Discard discard = discardService.create(request, actor);
        return ResponseEntity.status(201).body(DiscardResponse.from(discard));
    }

    @Operation(summary = "폐기 확정", description = "창고 재고 감소 + InventoryHistory 기록 + Discard CONFIRMED")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "확정 성공",
                content = @Content(schema = @Schema(implementation = DiscardResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "폐기 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{discardId}/confirm")
    public ResponseEntity<DiscardResponse> confirm(
            @Parameter(description = "폐기 ID", example = "1") @PathVariable Long discardId,
            @AuthenticationPrincipal User actor
    ) {
        Discard discard = discardService.confirm(discardId, actor);
        return ResponseEntity.ok(DiscardResponse.from(discard));
    }

    @Operation(summary = "폐기 취소", description = "CREATED 상태에서만 취소 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "취소 성공",
                content = @Content(schema = @Schema(implementation = DiscardResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "폐기 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{discardId}/cancel")
    public ResponseEntity<DiscardResponse> cancel(
            @Parameter(description = "폐기 ID", example = "1") @PathVariable Long discardId) {
        Discard discard = discardService.cancel(discardId);
        return ResponseEntity.ok(DiscardResponse.from(discard));
    }

    @Operation(summary = "폐기 단건 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = DiscardResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "폐기 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{discardId}")
    public ResponseEntity<DiscardResponse> get(
            @Parameter(description = "폐기 ID", example = "1") @PathVariable Long discardId) {
        Discard discard = discardService.get(discardId);
        return ResponseEntity.ok(DiscardResponse.from(discard));
    }

    @Operation(summary = "폐기 목록 조회/검색")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = DiscardListResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<DiscardListResponse> list(
            @Parameter(description = "매장 ID (선택)") @RequestParam(required = false) Long storeId,
            @Parameter(description = "매장 통합 검색어 (선택)") @RequestParam(required = false) String storeKeyword,
            @Parameter(description = "창고 검색어 (선택)") @RequestParam(required = false) String warehouseKeyword,
            @Parameter(description = "상품 검색어 (선택)") @RequestParam(required = false) String productKeyword,
            @Parameter(description = "창고 ID (선택)") @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "상태 (선택): CREATED/CONFIRMED/CANCELED") @RequestParam(required = false) String status,
            @Parameter(description = "생성일 시작 (yyyy-MM-dd)") @RequestParam(required = false) String from,
            @Parameter(description = "생성일 종료 (yyyy-MM-dd)") @RequestParam(required = false) String to,
            @Parameter(description = "폐기일 시작 (yyyy-MM-dd)") @RequestParam(required = false) String discardedFrom,
            @Parameter(description = "폐기일 종료 (yyyy-MM-dd)") @RequestParam(required = false) String discardedTo,
            @Parameter(description = "페이지 (0부터)", example = "0") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        return ResponseEntity.ok(discardService.list(
                storeId, storeKeyword, warehouseKeyword, productKeyword,
                warehouseId, status, from, to, discardedFrom, discardedTo, page, size
        ));
    }
}