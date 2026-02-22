package com.forerp.erp.dashboard.controller;

import com.forerp.erp.dashboard.dto.DashboardAlertsResponse;
import com.forerp.erp.dashboard.dto.DashboardRecentOrderItemResponse;
import com.forerp.erp.dashboard.dto.DashboardSummaryResponse;
import com.forerp.erp.dashboard.service.DashboardService;
import com.forerp.erp.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(@AuthenticationPrincipal User actor) {
        return ResponseEntity.ok(dashboardService.getSummary(actor));
    }

    @GetMapping("/alerts")
    public ResponseEntity<DashboardAlertsResponse> getAlerts(@AuthenticationPrincipal User actor) {
        return ResponseEntity.ok(dashboardService.getAlerts(actor));
    }

    @GetMapping("/recent-orders")
    public ResponseEntity<List<DashboardRecentOrderItemResponse>> getRecentOrders(
            @AuthenticationPrincipal User actor,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(dashboardService.getRecentOrders(actor, limit));
    }
}
