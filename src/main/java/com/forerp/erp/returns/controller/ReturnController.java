package com.forerp.erp.returns.controller;

import com.forerp.erp.returns.dto.ReturnListResponse;
import com.forerp.erp.returns.dto.ReturnProcessRequest;
import com.forerp.erp.returns.dto.ReturnResponse;
import com.forerp.erp.returns.service.ReturnService;
import com.forerp.erp.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Returns", description = "주문 반품 처리 API")
@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;

    @Operation(summary = "반품 처리", description = "출고 이후 주문에 대해 전체 반품(전체 환불)을 처리합니다.")
    @PostMapping
    public ResponseEntity<ReturnResponse> process(
            @AuthenticationPrincipal User actor,
            @Valid @RequestBody ReturnProcessRequest request
    ) {
        return ResponseEntity.ok(returnService.process(actor, request));
    }

    @Operation(summary = "반품 목록 조회")
    @GetMapping
    public ResponseEntity<ReturnListResponse> list(
            @AuthenticationPrincipal User actor,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(returnService.list(actor, storeId, status, from, to, page, size));
    }

    @Operation(summary = "반품 상세 조회")
    @GetMapping("/{returnId}")
    public ResponseEntity<ReturnResponse> get(
            @AuthenticationPrincipal User actor,
            @PathVariable Long returnId
    ) {
        return ResponseEntity.ok(returnService.get(actor, returnId));
    }

    @Operation(summary = "주문별 반품 조회")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ReturnResponse> getByOrderId(
            @AuthenticationPrincipal User actor,
            @PathVariable Long orderId
    ) {
        return returnService.getByOrderId(actor, orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

