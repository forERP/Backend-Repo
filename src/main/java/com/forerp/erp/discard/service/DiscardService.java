package com.forerp.erp.discard.service;

import com.forerp.erp.discard.domain.Discard;
import com.forerp.erp.discard.domain.DiscardItem;
import com.forerp.erp.discard.domain.DiscardStatus;
import com.forerp.erp.discard.repository.DiscardRepository;
import com.forerp.erp.inventory.domain.InventoryHistory;
import com.forerp.erp.inventory.repository.InventoryHistoryRepository;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DiscardService {

    private final DiscardRepository discardRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;

    /* ===== 폐기 생성 ===== */
    public Discard createDiscard(
            Store store,
            String reason,
            User actor,
            List<DiscardItem> items
    ) {
        Discard discard = Discard.create(store, reason, actor, items);
        return discardRepository.save(discard);
    }

    /* ===== 폐기 확정 ===== */
    public void confirmDiscard(Long discardId, User actor) {
        Discard discard = discardRepository.findById(discardId)
                .orElseThrow(() -> new IllegalArgumentException("폐기 내역을 찾을 수 없습니다."));

        if (discard.getStatus() == DiscardStatus.CONFIRMED) {
            throw new IllegalStateException("이미 확정된 폐기입니다.");
        }

        // 재고 차감 + 히스토리 생성
        discard.getItems().forEach(item -> {
            InventoryHistory history = item.discard(actor);
            inventoryHistoryRepository.save(history);
        });

        discard.confirm();
    }
}