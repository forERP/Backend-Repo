package com.forerp.erp.inbound.service;

import com.forerp.erp.common.query.QueryParamParser;
import com.forerp.erp.inbound.domain.Inbound;
import com.forerp.erp.inbound.domain.InboundStatus;
import com.forerp.erp.inbound.dto.InboundCreateRequest;
import com.forerp.erp.inbound.dto.InboundListResponse;
import com.forerp.erp.inbound.repository.InboundRepository;
import com.forerp.erp.inbound.service.support.InboundBuilder;
import com.forerp.erp.inbound.service.support.InboundLoader;
import com.forerp.erp.inventory.domain.InventoryHistory;
import com.forerp.erp.inventory.repository.InventoryHistoryRepository;
import com.forerp.erp.purchase_order.repository.PurchaseOrderRepository;
import com.forerp.erp.shipment.domain.Shipment;
import com.forerp.erp.shipment.domain.ShipmentStatus;
import com.forerp.erp.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InboundService {

    private final InboundRepository inboundRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    private final InboundLoader loader;
    private final InboundBuilder builder;

    /* 입고 생성 */
    public Inbound createInbound(InboundCreateRequest req) {
        Inbound inbound = builder.buildInboundAggregate(req);
        return inboundRepository.save(inbound);
    }

    /* 배송 출발 */
    public Shipment departShipment(Long inboundId, String carrier, String trackingNumber) {
        Inbound inbound = loader.loadInbound(inboundId);
        Shipment shipment = loader.requireShipment(inbound);

        shipment.depart(carrier, trackingNumber);
        return shipment;
    }

    /* 배송 도착 → 입고 확정 */
    public Inbound confirmInbound(Long inboundId, User actor) {
        Inbound inbound = loader.loadInbound(inboundId);
        Shipment shipment = loader.requireShipment(inbound);

        shipment.arrive();

        inbound.getItems().forEach(item -> {
            InventoryHistory history = item.receive(actor);
            inventoryHistoryRepository.save(history);
        });

        inbound.confirm();

        // 발주 닫기
        inbound.getPurchaseOrder().markReceived();
        purchaseOrderRepository.save(inbound.getPurchaseOrder());

        return inbound;
    }

    /* 입고 취소 (출발 전까지만 가능) */
    public Inbound cancelInbound(Long inboundId) {
        Inbound inbound = loader.loadInbound(inboundId);

        Shipment shipment = inbound.getShipment();
        if (shipment != null && shipment.getStatus() != ShipmentStatus.READY) {
            throw new IllegalStateException("배송 출발 이후에는 입고 취소가 불가능합니다.");
        }

        inbound.cancel();
        return inbound;
    }

    /* 입고 단건 조회 */
    @Transactional(readOnly = true)
    public Inbound getInbound(Long inboundId) {
        return loader.loadInbound(inboundId);
    }

    /* 입고 목록 조회 */
    @Transactional(readOnly = true)
    public InboundListResponse listInbounds(
            Long storeId,
            String status,
            String from,
            String to,
            int page,
            int size
    ) {
        InboundStatus st = QueryParamParser.parseEnumOrNull(status, InboundStatus.class, "status");
        LocalDateTime fromDt = QueryParamParser.parseFromDate(from);
        LocalDateTime toDt = QueryParamParser.parseToDateExclusive(to);

        PageRequest pageable = PageRequest.of(page, size);
        Page<Inbound> result = inboundRepository.search(storeId, st, fromDt, toDt, pageable);

        List<InboundListResponse.InboundListItem> content = result.getContent().stream()
                .map(i -> new InboundListResponse.InboundListItem(
                        i.getId(),
                        i.getStore().getId(),
                        i.getWarehouse().getId(),
                        i.getStatus().name(),
                        i.getCreatedAt(),
                        i.getShipment() == null ? null : i.getShipment().getStatus().name()
                ))
                .toList();

        return new InboundListResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}