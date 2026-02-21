package com.forerp.erp.discard.service;

import com.forerp.erp.auditlog.AuditLogAction;
import com.forerp.erp.auditlog.AuditLogService;
import com.forerp.erp.auditlog.AuditLogTargetType;
import com.forerp.erp.common.query.QueryParamParser;
import com.forerp.erp.discard.domain.Discard;
import com.forerp.erp.discard.domain.DiscardStatus;
import com.forerp.erp.discard.dto.DiscardCreateRequest;
import com.forerp.erp.discard.dto.DiscardListResponse;
import com.forerp.erp.discard.repository.DiscardRepository;
import com.forerp.erp.discard.service.support.DiscardBuilder;
import com.forerp.erp.discard.service.support.DiscardLoader;
import com.forerp.erp.inventory.domain.InventoryHistory;
import com.forerp.erp.inventory.repository.InventoryHistoryRepository;
import com.forerp.erp.realtime.service.RealtimeEventService;
import com.forerp.erp.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DiscardService {

    private final DiscardRepository discardRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;
    private final RealtimeEventService realtimeEventService;
    private final AuditLogService auditLogService;

    private final DiscardLoader loader;
    private final DiscardBuilder builder;

    /* ===== 폐기 생성 ===== */
    public Discard create(DiscardCreateRequest req, User actor) {
        Discard discard = builder.buildDiscardAggregate(req, actor);
        return discardRepository.save(discard);
    }

    /* ===== 폐기 확정(재고 감소 + 로그 저장 + 상태 변경) ===== */
    public Discard confirm(Long discardId, User actor) {
        Discard discard = loader.loadDiscardDetail(discardId);

        // 상태 가드
        discard.requireCreated();
        discard.requireNotCanceled();

        discard.getItems().forEach(item -> {
            InventoryHistory history = item.discard(actor);
            inventoryHistoryRepository.save(history);
        });

        discard.confirm();
        realtimeEventService.publishInventoryChanged(discard.getStore().getId(), "discard_confirmed");
        auditLogService.logActionSafely(actor, AuditLogAction.DISCARD_CONFIRM, AuditLogTargetType.DISCARD, discard.getId());
        return discard;
    }

    /* ===== 폐기 취소(작성 단계에서만) ===== */
    public Discard cancel(Long discardId) {
        Discard discard = loader.loadDiscardDetail(discardId);
        discard.cancel();
        auditLogService.logCurrentUserAction(AuditLogAction.DISCARD_CANCEL, AuditLogTargetType.DISCARD, discard.getId());
        return discard;
    }

    /* ===== 폐기 단건 조회 ===== */
    @Transactional(readOnly = true)
    public Discard get(Long discardId) {
        return loader.loadDiscardDetail(discardId);
    }

    /* ===== 폐기 목록 조회/검색 ===== */
    @Transactional(readOnly = true)
    public DiscardListResponse list(
            Long storeId,
            String storeKeyword,
            String warehouseKeyword,
            String productKeyword,
            Long warehouseId,
            String status,
            String from,
            String to,
            String discardedFrom,
            String discardedTo,
            int page,
            int size
    ) {
        DiscardStatus st = QueryParamParser.parseEnumOrNull(status, DiscardStatus.class, "status");
        LocalDateTime fromDt = QueryParamParser.parseFromDate(from);
        LocalDateTime toDt = QueryParamParser.parseToDateExclusive(to);
        LocalDateTime discardedFromDt = QueryParamParser.parseFromDate(discardedFrom);
        LocalDateTime discardedToDt = QueryParamParser.parseToDateExclusive(discardedTo);

        PageRequest pageable = PageRequest.of(page, size);
        Page<Discard> result = discardRepository.search(
                storeId,
                normalize(storeKeyword),
                normalize(warehouseKeyword),
                normalize(productKeyword),
                warehouseId,
                st,
                fromDt,
                toDt,
                discardedFromDt,
                discardedToDt,
                pageable
        );

        List<DiscardListResponse.DiscardListItem> content = result.getContent().stream()
                .map(d -> new DiscardListResponse.DiscardListItem(
                        d.getId(),
                        d.getStore().getId(),
                        d.getStore().getName(),
                        d.getStore().getStoreCode(),
                        d.getWarehouse().getId(),
                        d.getWarehouse().getName(),
                        d.getWarehouse().getCode(),
                        d.getCreatedBy() == null ? null : d.getCreatedBy().getId(),
                        d.getCreatedBy() == null ? null : d.getCreatedBy().getName(),
                        d.getStatus().name(),
                        d.getReason(),
                        d.getCreatedAt(),
                        d.getDiscardedAt()
                ))
                .toList();

        return new DiscardListResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
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
