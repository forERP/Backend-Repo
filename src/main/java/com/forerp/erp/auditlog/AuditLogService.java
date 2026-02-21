package com.forerp.erp.auditlog;

import com.forerp.erp.common.jwt.SecurityUtil;
import com.forerp.erp.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final SecurityUtil securityUtil;

    public void logAction(User actor, String action, String targetType, Long targetId){
        if (actor == null || action == null || targetType == null || targetId == null) {
            return;
        }

        AuditLog auditLog = AuditLog.builder()
                .actor(actor)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .build();
        auditLogRepository.save(auditLog);
    }

    public void logActionSafely(User actor, String action, String targetType, Long targetId) {
        try {
            logAction(actor, action, targetType, targetId);
        } catch (Exception ignored) {
        }
    }

    public void logCurrentUserAction(String action, String targetType, Long targetId) {
        try {
            User actor = securityUtil.getCurrentUser();
            logAction(actor, action, targetType, targetId);
        } catch (Exception ignored) {
        }
    }
}
