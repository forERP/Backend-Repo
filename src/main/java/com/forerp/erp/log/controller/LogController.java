package com.forerp.erp.log.controller;

import com.forerp.erp.auditlog.AuditLogQueryService;
import com.forerp.erp.auditlog.dto.AdminLogListItemResponse;
import com.forerp.erp.common.exception.ApiErrorResponse;
import com.forerp.erp.inventory.dto.InventoryLogListItemResponse;
import com.forerp.erp.inventory.service.InventoryLogQueryService;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.domain.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "감사 로그", description = "관리자 감사 로그 및 재고 이력 조회 API (HQ_ADMIN 전용)")
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class LogController {

    private final AuditLogQueryService auditLogQueryService;
    private final InventoryLogQueryService inventoryLogQueryService;

    @Operation(summary = "관리자 행위 로그 검색 조회", description = "HQ_ADMIN 권한 전용")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = AdminLogListItemResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "권한 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/admin")
    public ResponseEntity<Page<AdminLogListItemResponse>> searchAdminLogs(
            @AuthenticationPrincipal User actor,
            @Parameter(description = "행위자 검색어 (선택)") @RequestParam(required = false) String actorKeyword,
            @Parameter(description = "행위 유형 (선택)") @RequestParam(required = false) String action,
            @Parameter(description = "대상 유형 (선택)") @RequestParam(required = false) String targetType,
            @Parameter(description = "조회 시작일 (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "조회 종료일 (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        assertLogViewAllowed(actor);
        return ResponseEntity.ok(auditLogQueryService.search(actorKeyword, action, targetType, from, to, pageable));
    }

    @Operation(summary = "재고 이력 로그 검색 조회", description = "HQ_ADMIN 권한 전용")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = InventoryLogListItemResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "권한 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/inventory")
    public ResponseEntity<Page<InventoryLogListItemResponse>> searchInventoryLogs(
            @AuthenticationPrincipal User actor,
            @Parameter(description = "이벤트 유형 (선택)") @RequestParam(required = false) String eventType,
            @Parameter(description = "매장 검색어 (선택)") @RequestParam(required = false) String storeKeyword,
            @Parameter(description = "창고 검색어 (선택)") @RequestParam(required = false) String warehouseKeyword,
            @Parameter(description = "상품 검색어 (선택)") @RequestParam(required = false) String productKeyword,
            @Parameter(description = "행위자 검색어 (선택)") @RequestParam(required = false) String actorKeyword,
            @Parameter(description = "조회 시작일 (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "조회 종료일 (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        assertLogViewAllowed(actor);
        return ResponseEntity.ok(inventoryLogQueryService.search(
                eventType, storeKeyword, warehouseKeyword, productKeyword, actorKeyword, from, to, pageable));
    }

    private void assertLogViewAllowed(User actor) {
        if (actor == null || actor.getRole() != UserRole.HQ_ADMIN) {
            throw new AccessDeniedException("Only HQ admins can view logs.");
        }
    }
}