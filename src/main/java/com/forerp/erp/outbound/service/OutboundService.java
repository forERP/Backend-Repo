package com.forerp.erp.outbound.service;

import com.forerp.erp.inventory.domain.InventoryHistory;
import com.forerp.erp.inventory.repository.InventoryHistoryRepository;
import com.forerp.erp.order.domain.Order;
import com.forerp.erp.outbound.domain.Outbound;
import com.forerp.erp.outbound.domain.OutboundItem;
import com.forerp.erp.outbound.domain.OutboundStatus;
import com.forerp.erp.outbound.repository.OutboundRepository;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OutboundService {

    private final OutboundRepository outboundRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;

    /* ===== 출고 생성 ===== */
    public Outbound createOutbound(
            Order order,
            Store store,
            List<OutboundItem> items
    ) {
        Outbound outbound = Outbound.create(order, store, items);
        return outboundRepository.save(outbound);
    }

    /* ===== 출고 확정 ===== */
    public void confirmOutbound(Long outboundId, User actor) {
        Outbound outbound = outboundRepository.findById(outboundId)
                .orElseThrow(() -> new IllegalArgumentException("출고를 찾을 수 없습니다."));

        if (outbound.getStatus() == OutboundStatus.CONFIRMED) {
            throw new IllegalStateException("이미 확정된 출고입니다.");
        }

        // 재고 차감 + 히스토리 생성
        outbound.getItems().forEach(item -> {
            InventoryHistory history = item.ship(actor);
            inventoryHistoryRepository.save(history);
        });

        outbound.confirm(); // status 변경
    }
}