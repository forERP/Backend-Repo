package com.forerp.erp.inbound.service;

import com.forerp.erp.inbound.domain.Inbound;
import com.forerp.erp.inbound.domain.InboundItem;
import com.forerp.erp.inbound.domain.InboundStatus;
import com.forerp.erp.inbound.repository.InboundRepository;
import com.forerp.erp.inventory.domain.InventoryHistory;
import com.forerp.erp.inventory.repository.InventoryHistoryRepository;
import com.forerp.erp.purchase_order.domain.PurchaseOrder;
import com.forerp.erp.purchase_order.repository.PurchaseOrderRepository;
import com.forerp.erp.shipment.domain.Shipment;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.warehouse.domain.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InboundService {

    private final InboundRepository inboundRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    /* ===== 입고 생성 ===== */
    public Inbound createInbound(
            PurchaseOrder purchaseOrder,
            Store store,
            Warehouse warehouse,
            List<InboundItem> items
    ) {
        Inbound inbound = Inbound.create(purchaseOrder, store, warehouse, items);

        Shipment.createForInbound(inbound);

        return inboundRepository.save(inbound);
    }

    /* ===== 배송 출발 ===== */
    public void departShipment(Long inboundId, String carrier, String trackingNumber) {
        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new IllegalArgumentException("입고를 찾을 수 없습니다."));

        inbound.getShipment().depart(carrier, trackingNumber);
        inbound.changeStatus(InboundStatus.SHIPPING);
    }

    /* ===== 배송 도착 → 입고 확정 ===== */
    public void confirmInbound(Long inboundId, User actor) {
        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new IllegalArgumentException("입고를 찾을 수 없습니다."));

        Shipment shipment = inbound.getShipment();
        shipment.arrive();

        inbound.getItems().forEach(item -> {
            InventoryHistory history = item.receive(actor);
            inventoryHistoryRepository.save(history);
        });

        inbound.confirm();

        // 발주도 닫아주기
        PurchaseOrder po = inbound.getPurchaseOrder();
        po.markReceived();
        purchaseOrderRepository.save(po);
    }
}