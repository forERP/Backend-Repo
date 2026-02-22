package com.forerp.erp.payment.controller;

import com.forerp.erp.common.exception.ApiErrorResponse;
import com.forerp.erp.payment.dto.PosOrderDetailResponse;
import com.forerp.erp.payment.dto.PosOrderListResponse;
import com.forerp.erp.payment.service.PosOrderWorkflowService;
import com.forerp.erp.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "POS 주문", description = "POS 주문 준비 및 이후 플로우 API")
@RestController
@RequestMapping("/api/pos/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PosOrderController {

    private final PosOrderWorkflowService posOrderWorkflowService;

    @Operation(summary = "POS 주문 목록 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = PosOrderListResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PosOrderListResponse> list(
            @AuthenticationPrincipal User actor,
            @Parameter(description = "상태 (선택)") @RequestParam(required = false) String status,
            @Parameter(description = "페이지 (0부터)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(posOrderWorkflowService.list(actor, status, page, size));
    }

    @Operation(summary = "POS 주문 상세 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = PosOrderDetailResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "주문 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<PosOrderDetailResponse> detail(
            @AuthenticationPrincipal User actor,
            @Parameter(description = "주문 ID", example = "1") @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(posOrderWorkflowService.getDetail(actor, orderId));
    }

    @Operation(summary = "주문 준비 처리 (조리 완료)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "처리 성공",
                content = @Content(schema = @Schema(implementation = PosOrderDetailResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "주문 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{orderId}/prepare")
    public ResponseEntity<PosOrderDetailResponse> prepare(
            @AuthenticationPrincipal User actor,
            @Parameter(description = "주문 ID", example = "1") @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(posOrderWorkflowService.prepare(actor, orderId));
    }

    @Operation(summary = "조리 확정 처리 (필요 시 조리완료 처리)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "처리 성공",
                content = @Content(schema = @Schema(implementation = PosOrderDetailResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "주문 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<PosOrderDetailResponse> confirm(
            @AuthenticationPrincipal User actor,
            @Parameter(description = "주문 ID", example = "1") @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(posOrderWorkflowService.confirm(actor, orderId));
    }
}