package com.forerp.erp.common.audit;

import com.forerp.erp.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void logAction(User actor, String action, String targetType, Long targetId){
        AuditLog auditLog = AuditLog.builder()
                .actor(actor)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .build();
        auditLogRepository.save(auditLog);
    }
}
