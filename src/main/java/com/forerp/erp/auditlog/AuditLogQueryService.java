package com.forerp.erp.auditlog;

import com.forerp.erp.auditlog.dto.AdminLogListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogQueryService {

    private final AuditLogRepository auditLogRepository;

    public Page<AdminLogListItemResponse> search(
            String actorKeyword,
            String action,
            String targetType,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {
        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.plusDays(1).atStartOfDay();

        if (fromDateTime != null && toDateTime != null && toDateTime.isBefore(fromDateTime)) {
            throw new IllegalArgumentException("to must be on or after from");
        }

        return auditLogRepository.search(
                normalize(actorKeyword),
                normalize(action),
                normalize(targetType),
                fromDateTime,
                toDateTime,
                pageable
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
