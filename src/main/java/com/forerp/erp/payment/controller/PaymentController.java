package com.forerp.erp.payment.controller;

import com.forerp.erp.payment.dto.PaymentCancelRequest;
import com.forerp.erp.payment.dto.PaymentCancelResponse;
import com.forerp.erp.payment.dto.PaymentConfirmRequest;
import com.forerp.erp.payment.dto.PaymentConfirmResponse;
import com.forerp.erp.payment.dto.PaymentListResponse;
import com.forerp.erp.payment.dto.PaymentPrepareRequest;
import com.forerp.erp.payment.dto.PaymentPrepareResponse;
import com.forerp.erp.payment.dto.PaymentSummaryResponse;
import com.forerp.erp.payment.service.PaymentService;
import com.forerp.erp.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

@Tag(name = "Payment", description = "Toss 결제 연동 API")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 준비", description = "결제 요청 전 금액/창고를 확정하고 merchantOrderId를 발급합니다.")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PaymentPrepareResponse.class)))
    @PostMapping("/prepare")
    public ResponseEntity<PaymentPrepareResponse> prepare(
            @AuthenticationPrincipal User actor,
            @Valid @RequestBody PaymentPrepareRequest request
    ) {
        return ResponseEntity.ok(paymentService.prepare(actor, request));
    }

    @Operation(summary = "결제 승인", description = "Toss 승인 완료 후 주문을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PaymentConfirmResponse.class)))
    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirm(
            @AuthenticationPrincipal User actor,
            @Valid @RequestBody PaymentConfirmRequest request
    ) {
        return ResponseEntity.ok(paymentService.confirm(actor, request));
    }

    @Operation(summary = "결제 취소/환불", description = "부분취소/전체취소를 처리합니다.")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PaymentCancelResponse.class)))
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentCancelResponse> cancel(
            @AuthenticationPrincipal User actor,
            @PathVariable Long paymentId,
            @Valid @RequestBody PaymentCancelRequest request
    ) {
        return ResponseEntity.ok(paymentService.cancel(actor, paymentId, request));
    }

    @Operation(summary = "결제 단건 조회")
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentSummaryResponse> get(
            @AuthenticationPrincipal User actor,
            @PathVariable Long paymentId
    ) {
        return ResponseEntity.ok(paymentService.get(actor, paymentId));
    }

    @Operation(summary = "주문 기준 결제 조회")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentSummaryResponse> getByOrderId(
            @AuthenticationPrincipal User actor,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(paymentService.getByOrderId(actor, orderId));
    }

    @Operation(summary = "결제 목록 조회")
    @GetMapping
    public ResponseEntity<PaymentListResponse> list(
            @AuthenticationPrincipal User actor,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(paymentService.list(actor, storeId, status, from, to, page, size));
    }

    @Operation(summary = "Toss 결제 웹훅", description = "결제 상태 변경 이벤트를 수신합니다.")
    @PostMapping("/webhook/toss")
    public ResponseEntity<Void> tossWebhook(@RequestBody String body) {
        paymentService.handleWebhook(body);
        return ResponseEntity.ok().build();
    }
}

