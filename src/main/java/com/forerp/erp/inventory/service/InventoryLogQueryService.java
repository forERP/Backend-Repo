package com.forerp.erp.inventory.service;

import com.forerp.erp.inventory.domain.RefType;
import com.forerp.erp.inventory.dto.InventoryLogListItemResponse;
import com.forerp.erp.inventory.repository.InventoryHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryLogQueryService {

    private static final Set<RefType> SUPPORTED_EVENTS = Set.of(
            RefType.INBOUND,
            RefType.OUTBOUND,
            RefType.RETURN,
            RefType.DISCARD,
            RefType.ADJUST
    );

    private final InventoryHistoryRepository inventoryHistoryRepository;

    public Page<InventoryLogListItemResponse> search(
            String eventType,
            String storeKeyword,
            String warehouseKeyword,
            String productKeyword,
            String actorKeyword,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {
        RefType parsedEventType = parseEventType(eventType);
        List<RefType> eventTypes = parsedEventType == null
                ? List.copyOf(SUPPORTED_EVENTS)
                : List.of(parsedEventType);

        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.plusDays(1).atStartOfDay();
        if (fromDateTime != null && toDateTime != null && toDateTime.isBefore(fromDateTime)) {
            throw new IllegalArgumentException("to must be on or after from");
        }

        return inventoryHistoryRepository.searchLogs(
                eventTypes,
                normalize(storeKeyword),
                normalize(warehouseKeyword),
                normalize(productKeyword),
                normalize(actorKeyword),
                fromDateTime,
                toDateTime,
                pageable
        );
    }

    private RefType parseEventType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        RefType parsed;
        try {
            parsed = RefType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid inventory log eventType: " + value);
        }

        if (!SUPPORTED_EVENTS.contains(parsed)) {
            throw new IllegalArgumentException("Unsupported inventory log eventType: " + value);
        }
        return parsed;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
