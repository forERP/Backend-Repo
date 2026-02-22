package com.forerp.erp.returns.controller;

import com.forerp.erp.common.exception.ApiErrorResponse;
import com.forerp.erp.returns.dto.ReturnListResponse;
import com.forerp.erp.returns.dto.ReturnProcessRequest;
import com.forerp.erp.returns.dto.ReturnResponse;
import com.forerp.erp.returns.service.ReturnService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "반품", description = "반품 처리 API")
@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ReturnController {

    private final ReturnService returnService;

    @Operation(summary = "반품 처리", description = "주문 취소 후 고객으로부터의 반품(전체 반품)을 처리합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "처리 성공",
                content = @Content(schema = @Schema(implementation = ReturnResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ReturnResponse> process(
            @AuthenticationPrincipal User actor,
            @Valid @RequestBody ReturnProcessRequest request
    ) {
        return ResponseEntity.ok(returnService.process(actor, request));
    }

    @Operation(summary = "반품 목록 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = ReturnListResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ReturnListResponse> list(
            @AuthenticationPrincipal User actor,
            @Parameter(description = "매장 ID (선택)") @RequestParam(required = false) Long storeId,
            @Parameter(description = "상태 (선택)") @RequestParam(required = false) String status,
            @Parameter(description = "조회 시작일 (yyyy-MM-dd)") @RequestParam(required = false) String from,
            @Parameter(description = "조회 종료일 (yyyy-MM-dd)") @RequestParam(required = false) String to,
            @Parameter(description = "페이지 (0부터)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(returnService.list(actor, storeId, status, from, to, page, size));
    }

    @Operation(summary = "반품 단건 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = ReturnResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "반품 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{returnId}")
    public ResponseEntity<ReturnResponse> get(
            @AuthenticationPrincipal User actor,
            @Parameter(description = "반품 ID", example = "1") @PathVariable Long returnId
    ) {
        return ResponseEntity.ok(returnService.get(actor, returnId));
    }

    @Operation(summary = "주문별 반품 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = ReturnResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "반품 없음")
    })
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ReturnResponse> getByOrderId(
            @AuthenticationPrincipal User actor,
            @Parameter(description = "주문 ID", example = "1") @PathVariable Long orderId
    ) {
        return returnService.getByOrderId(actor, orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}