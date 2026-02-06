package com.forerp.erp.order.controller;

import com.forerp.erp.order.domain.Order;
import com.forerp.erp.order.dto.OrderCreateRequest;
import com.forerp.erp.order.dto.OrderListResponse;
import com.forerp.erp.order.dto.OrderResponse;
import com.forerp.erp.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Order", description = "주문 API (POS)")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "주문 생성", description = "주문 + 주문아이템 생성")
    @ApiResponse(responseCode = "201", description = "Created",
            content = @Content(schema = @Schema(implementation = OrderResponse.class)))
    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        Order order = orderService.create(request);
        return ResponseEntity.status(201).body(OrderResponse.from(order));
    }

    @Operation(summary = "주문 단건 조회")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = OrderResponse.class)))
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> get(@PathVariable Long orderId) {
        return ResponseEntity.ok(OrderResponse.from(orderService.get(orderId)));
    }

    @Operation(summary = "주문 목록 조회/검색")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = OrderListResponse.class)))
    @GetMapping
    public ResponseEntity<OrderListResponse> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        return ResponseEntity.ok(orderService.list(storeId, status, from, to, page, size));
    }

    @Operation(summary = "주문 취소", description = "출고 생성 전까지만 취소 가능")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = OrderResponse.class)))
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long orderId) {
        return ResponseEntity.ok(OrderResponse.from(orderService.cancel(orderId)));
    }
}