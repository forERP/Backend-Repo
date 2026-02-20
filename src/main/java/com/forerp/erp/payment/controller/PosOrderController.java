package com.forerp.erp.payment.controller;

import com.forerp.erp.payment.dto.PosOrderDetailResponse;
import com.forerp.erp.payment.dto.PosOrderListResponse;
import com.forerp.erp.payment.service.PosOrderWorkflowService;
import com.forerp.erp.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "POS Order Workflow", description = "POS 주문 준비/출고 워크플로 API")
@RestController
@RequestMapping("/api/pos/orders")
@RequiredArgsConstructor
public class PosOrderController {

    private final PosOrderWorkflowService posOrderWorkflowService;

    @Operation(summary = "POS 주문 목록 조회")
    @GetMapping
    public ResponseEntity<PosOrderListResponse> list(
            @AuthenticationPrincipal User actor,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(posOrderWorkflowService.list(actor, status, page, size));
    }

    @Operation(summary = "POS 주문 상세 조회")
    @GetMapping("/{orderId}")
    public ResponseEntity<PosOrderDetailResponse> detail(
            @AuthenticationPrincipal User actor,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(posOrderWorkflowService.getDetail(actor, orderId));
    }

    @Operation(summary = "주문 준비 처리 (출고 생성)")
    @PostMapping("/{orderId}/prepare")
    public ResponseEntity<PosOrderDetailResponse> prepare(
            @AuthenticationPrincipal User actor,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(posOrderWorkflowService.prepare(actor, orderId));
    }

    @Operation(summary = "출고 확정 처리 (필요 시 즉시 출고완료)")
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<PosOrderDetailResponse> confirm(
            @AuthenticationPrincipal User actor,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(posOrderWorkflowService.confirm(actor, orderId));
    }
}

