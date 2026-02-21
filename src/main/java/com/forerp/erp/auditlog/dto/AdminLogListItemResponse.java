package com.forerp.erp.auditlog.dto;

import com.forerp.erp.user.domain.UserRole;

import java.time.LocalDateTime;

public record AdminLogListItemResponse(
        Long logId,
        Long actorUserId,
        String actorName,
        String actorLoginId,
        String actorEmployeeCode,
        UserRole actorRole,
        Long actorStoreId,
        String actorStoreName,
        String actorStoreCode,
        String action,
        String targetType,
        Long targetId,
        LocalDateTime actionAt
) {
}
