package com.forerp.erp.discard.controller;

import com.forerp.erp.discard.domain.Discard;
import com.forerp.erp.discard.dto.DiscardCreateRequest;
import com.forerp.erp.discard.dto.DiscardListResponse;
import com.forerp.erp.discard.dto.DiscardResponse;
import com.forerp.erp.discard.service.DiscardService;
import com.forerp.erp.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Discard", description = "상품 폐기(재고 감소) API (관리자 + POS)")
@RestController
@RequestMapping("/api/discards")
public class DiscardController {

    private final DiscardService discardService;

    public DiscardController(DiscardService discardService) {
        this.discardService = discardService;
    }

    @Operation(summary = "폐기 생성", description = "폐기 문서를 생성합니다. (재고 감소는 확정 시점)")
    @ApiResponse(responseCode = "201", description = "Created",
            content = @Content(schema = @Schema(implementation = DiscardResponse.class)))
    @PostMapping
    public ResponseEntity<DiscardResponse> create(
            @Valid @RequestBody DiscardCreateRequest request,
            @AuthenticationPrincipal User actor
    ) {
        Discard discard = discardService.create(request, actor);
        return ResponseEntity.status(201).body(DiscardResponse.from(discard));
    }

    @Operation(summary = "폐기 확정", description = "재고 감소 + InventoryHistory 저장 + Discard CONFIRMED")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = DiscardResponse.class)))
    @PostMapping("/{discardId}/confirm")
    public ResponseEntity<DiscardResponse> confirm(
            @PathVariable Long discardId,
            @AuthenticationPrincipal User actor
    ) {
        Discard discard = discardService.confirm(discardId, actor);
        return ResponseEntity.ok(DiscardResponse.from(discard));
    }

    @Operation(summary = "폐기 취소", description = "CREATED 상태에서만 취소 가능합니다.")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = DiscardResponse.class)))
    @PostMapping("/{discardId}/cancel")
    public ResponseEntity<DiscardResponse> cancel(@PathVariable Long discardId) {
        Discard discard = discardService.cancel(discardId);
        return ResponseEntity.ok(DiscardResponse.from(discard));
    }

    @Operation(summary = "폐기 단건 조회")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = DiscardResponse.class)))
    @GetMapping("/{discardId}")
    public ResponseEntity<DiscardResponse> get(@PathVariable Long discardId) {
        Discard discard = discardService.get(discardId);
        return ResponseEntity.ok(DiscardResponse.from(discard));
    }

    @Operation(summary = "폐기 목록 조회/검색")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = DiscardListResponse.class)))
    @GetMapping
    public ResponseEntity<DiscardListResponse> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String storeKeyword,
            @RequestParam(required = false) String warehouseKeyword,
            @RequestParam(required = false) String productKeyword,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from, // yyyy-MM-dd
            @RequestParam(required = false) String to,   // yyyy-MM-dd
            @RequestParam(required = false) String discardedFrom, // yyyy-MM-dd
            @RequestParam(required = false) String discardedTo,   // yyyy-MM-dd
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        return ResponseEntity.ok(discardService.list(
                storeId,
                storeKeyword,
                warehouseKeyword,
                productKeyword,
                warehouseId,
                status,
                from,
                to,
                discardedFrom,
                discardedTo,
                page,
                size
        ));
    }
}
