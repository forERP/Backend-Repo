package com.forerp.erp.outbound.service;

import com.forerp.erp.inventory.domain.InventoryHistory;
import com.forerp.erp.inventory.repository.InventoryHistoryRepository;
import com.forerp.erp.order.domain.Order;
import com.forerp.erp.outbound.domain.Outbound;
import com.forerp.erp.outbound.domain.OutboundItem;
import com.forerp.erp.outbound.repository.OutboundRepository;
import com.forerp.erp.shipment.domain.Shipment;
import com.forerp.erp.shipment.domain.ShipmentStatus;
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
    public Outbound createOutbound(Order order, Store store, List<OutboundItem> items) {
        Outbound outbound = Outbound.create(order, store, items);
        Shipment.createForOutbound(outbound);
        return outboundRepository.save(outbound);
    }

    /* ===== 배송 출발 → 출고 확정 ===== */
    @Transactional
    public void confirmOutbound(Long outboundId, User actor, String carrier, String trackingNumber) {
        Outbound outbound = outboundRepository.findById(outboundId)
                .orElseThrow(() -> new IllegalArgumentException("출고를 찾을 수 없습니다."));

        Shipment shipment = outbound.getShipment();
        if (shipment == null) {
            throw new IllegalStateException("출고에 연결된 배송 정보가 없습니다.");
        }

        // 1) 배송 출발(송장 필수) - READY에서만 가능
        shipment.depart(carrier, trackingNumber);

        // 2) 재고 차감
        outbound.getItems().forEach(item -> {
            InventoryHistory history = item.ship(actor);
            inventoryHistoryRepository.save(history);
        });

        // 3) 출고 확정
        outbound.confirm();
    }

    /* ===== 출고 취소 ===== */
    public void cancelOutbound(Long outboundId) {
        Outbound outbound = outboundRepository.findById(outboundId)
                .orElseThrow(() -> new IllegalArgumentException("출고를 찾을 수 없습니다."));

        // 이미 배송 출발했으면 취소 금지
        Shipment shipment = outbound.getShipment();
        if (shipment != null && shipment.getStatus() != ShipmentStatus.READY) {
            throw new IllegalStateException("배송 출발 이후에는 출고 취소가 불가능합니다.");
        }

        outbound.cancel();
    }
}