package com.forerp.erp.order.controller;

import com.forerp.erp.common.exception.ApiErrorResponse;
import com.forerp.erp.order.domain.Order;
import com.forerp.erp.order.dto.OrderCreateRequest;
import com.forerp.erp.order.dto.OrderListResponse;
import com.forerp.erp.order.dto.OrderResponse;
import com.forerp.erp.order.service.OrderService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "주문", description = "매장 → 창고 발주 관리 API (POS/백오피스)")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "주문 생성", description = "매장에서 창고로 상품을 발주합니다. 주문 상품 목록 포함.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "주문 생성 성공",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 오류",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        Order order = orderService.create(request);
        return ResponseEntity.status(201).body(OrderResponse.from(order));
    }

    @Operation(summary = "주문 단건 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "주문 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> get(
            @Parameter(description = "주문 ID", example = "1") @PathVariable Long orderId) {
        return ResponseEntity.ok(OrderResponse.from(orderService.get(orderId)));
    }

    @Operation(summary = "주문 목록 조회 / 검색", description = "매장·창고·상태·날짜 조건으로 주문 목록을 페이지 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = OrderListResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<OrderListResponse> list(
            @Parameter(description = "매장 ID") @RequestParam(required = false) Long storeId,
            @Parameter(description = "매장 통합 키워드") @RequestParam(required = false) String storeKeyword,
            @Parameter(description = "매장 이름") @RequestParam(required = false) String storeName,
            @Parameter(description = "매장 코드") @RequestParam(required = false) String storeCode,
            @Parameter(description = "주문 상태 (PLACED/PREPARED/SHIPPED/ARRIVED/CANCELED)") @RequestParam(required = false) String status,
            @Parameter(description = "조회 시작일 (yyyy-MM-dd)", example = "2025-01-01") @RequestParam(required = false) String from,
            @Parameter(description = "조회 종료일 (yyyy-MM-dd)", example = "2025-01-31") @RequestParam(required = false) String to,
            @Parameter(description = "페이지 번호 (0부터)", example = "0") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        return ResponseEntity.ok(orderService.list(storeId, storeKeyword, storeName, storeCode, status, from, to, page, size));
    }

    @Operation(summary = "주문 취소", description = "입고 보고서 생성 이전 단계의 주문을 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "취소 성공",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "취소 불가 상태",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "주문 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancel(
            @Parameter(description = "주문 ID", example = "1") @PathVariable Long orderId) {
        return ResponseEntity.ok(OrderResponse.from(orderService.cancel(orderId)));
    }
}