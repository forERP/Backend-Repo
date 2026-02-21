package com.forerp.erp.auditlog;

import com.forerp.erp.common.jwt.SecurityUtil;
import com.forerp.erp.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogTxService auditLogTxService;
    private final SecurityUtil securityUtil;

    public void logAction(User actor, String action, String targetType, Long targetId){
        if (actor == null || action == null || targetType == null || targetId == null) {
            return;
        }

        auditLogTxService.save(actor, action, targetType, targetId);
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
