package com.forerp.erp.payment.controller;

import com.forerp.erp.common.exception.ApiErrorResponse;
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

@Tag(name = "결제", description = "Toss 결제 연동 API")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 준비", description = "결제 요청 전 주문/창고를 확정하고 merchantOrderId를 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "준비 성공",
                content = @Content(schema = @Schema(implementation = PaymentPrepareResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/prepare")
    public ResponseEntity<PaymentPrepareResponse> prepare(
            @AuthenticationPrincipal User actor,
            @Valid @RequestBody PaymentPrepareRequest request
    ) {
        return ResponseEntity.ok(paymentService.prepare(actor, request));
    }

    @Operation(summary = "결제 확인", description = "Toss 확인 완료 후 주문을 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "확인 성공",
                content = @Content(schema = @Schema(implementation = PaymentConfirmResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirm(
            @AuthenticationPrincipal User actor,
            @Valid @RequestBody PaymentConfirmRequest request
    ) {
        return ResponseEntity.ok(paymentService.confirm(actor, request));
    }

    @Operation(summary = "결제 취소/환불", description = "고객 요청에 따른 전체취소를 처리합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "취소/환불 성공",
                content = @Content(schema = @Schema(implementation = PaymentCancelResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "결제 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentCancelResponse> cancel(
            @AuthenticationPrincipal User actor,
            @Parameter(description = "결제 ID", example = "1") @PathVariable Long paymentId,
            @Valid @RequestBody PaymentCancelRequest request
    ) {
        return ResponseEntity.ok(paymentService.cancel(actor, paymentId, request));
    }

    @Operation(summary = "결제 단건 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = PaymentSummaryResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "결제 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentSummaryResponse> get(
            @AuthenticationPrincipal User actor,
            @Parameter(description = "결제 ID", example = "1") @PathVariable Long paymentId
    ) {
        return ResponseEntity.ok(paymentService.get(actor, paymentId));
    }

    @Operation(summary = "주문 기준 결제 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = PaymentSummaryResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "결제 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentSummaryResponse> getByOrderId(
            @AuthenticationPrincipal User actor,
            @Parameter(description = "주문 ID", example = "1") @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(paymentService.getByOrderId(actor, orderId));
    }

    @Operation(summary = "결제 목록 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = PaymentListResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PaymentListResponse> list(
            @AuthenticationPrincipal User actor,
            @Parameter(description = "매장 ID (선택)") @RequestParam(required = false) Long storeId,
            @Parameter(description = "결제 상태 (선택)") @RequestParam(required = false) String status,
            @Parameter(description = "조회 시작일 (yyyy-MM-dd)") @RequestParam(required = false) String from,
            @Parameter(description = "조회 종료일 (yyyy-MM-dd)") @RequestParam(required = false) String to,
            @Parameter(description = "페이지 (0부터)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(paymentService.list(actor, storeId, status, from, to, page, size));
    }

    @Operation(summary = "Toss 결제 웹훅", description = "결제 상태 변경 이벤트를 수신합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "처리 성공")
    })
    @PostMapping("/webhook/toss")
    public ResponseEntity<Void> tossWebhook(@RequestBody String body) {
        paymentService.handleWebhook(body);
        return ResponseEntity.ok().build();
    }
}