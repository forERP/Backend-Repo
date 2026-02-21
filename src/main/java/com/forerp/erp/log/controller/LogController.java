package com.forerp.erp.log.controller;

import com.forerp.erp.auditlog.AuditLogQueryService;
import com.forerp.erp.auditlog.dto.AdminLogListItemResponse;
import com.forerp.erp.inventory.dto.InventoryLogListItemResponse;
import com.forerp.erp.inventory.service.InventoryLogQueryService;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.domain.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final AuditLogQueryService auditLogQueryService;
    private final InventoryLogQueryService inventoryLogQueryService;

    @GetMapping("/admin")
    public ResponseEntity<Page<AdminLogListItemResponse>> searchAdminLogs(
            @AuthenticationPrincipal User actor,
            @RequestParam(required = false) String actorKeyword,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        assertLogViewAllowed(actor);
        return ResponseEntity.ok(auditLogQueryService.search(
                actorKeyword,
                action,
                targetType,
                from,
                to,
                pageable
        ));
    }

    @GetMapping("/inventory")
    public ResponseEntity<Page<InventoryLogListItemResponse>> searchInventoryLogs(
            @AuthenticationPrincipal User actor,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String storeKeyword,
            @RequestParam(required = false) String warehouseKeyword,
            @RequestParam(required = false) String productKeyword,
            @RequestParam(required = false) String actorKeyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        assertLogViewAllowed(actor);
        return ResponseEntity.ok(inventoryLogQueryService.search(
                eventType,
                storeKeyword,
                warehouseKeyword,
                productKeyword,
                actorKeyword,
                from,
                to,
                pageable
        ));
    }

    private void assertLogViewAllowed(User actor) {
        if (actor == null || actor.getRole() != UserRole.HQ_ADMIN) {
            throw new AccessDeniedException("Only HQ admins can view logs.");
        }
    }
}
