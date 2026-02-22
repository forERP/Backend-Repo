package com.forerp.erp.dashboard.controller;

import com.forerp.erp.common.exception.ApiErrorResponse;
import com.forerp.erp.dashboard.dto.DashboardAlertsResponse;
import com.forerp.erp.dashboard.dto.DashboardRecentOrderItemResponse;
import com.forerp.erp.dashboard.dto.DashboardSummaryResponse;
import com.forerp.erp.dashboard.service.DashboardService;
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

import java.util.List;

@Tag(name = "대시보드", description = "대시보드 요약 정보 API")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "대시보드 요약 조회", description = "매출, 주문, 재고 현황 요약 정보를 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = DashboardSummaryResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(@AuthenticationPrincipal User actor) {
        return ResponseEntity.ok(dashboardService.getSummary(actor));
    }

    @Operation(summary = "대시보드 알림 조회", description = "재고 부족, 미처리 주문 등 알림 목록을 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = DashboardAlertsResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/alerts")
    public ResponseEntity<DashboardAlertsResponse> getAlerts(@AuthenticationPrincipal User actor) {
        return ResponseEntity.ok(dashboardService.getAlerts(actor));
    }

    @Operation(summary = "최근 주문 목록 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = DashboardRecentOrderItemResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/recent-orders")
    public ResponseEntity<List<DashboardRecentOrderItemResponse>> getRecentOrders(
            @AuthenticationPrincipal User actor,
            @Parameter(description = "최대 반환 개수 (선택)", example = "10") @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(dashboardService.getRecentOrders(actor, limit));
    }
}