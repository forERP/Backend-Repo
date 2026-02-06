package com.forerp.erp.outbound.service;

import com.forerp.erp.common.query.QueryParamParser;
import com.forerp.erp.inventory.domain.InventoryHistory;
import com.forerp.erp.inventory.repository.InventoryHistoryRepository;
import com.forerp.erp.outbound.domain.Outbound;
import com.forerp.erp.outbound.domain.OutboundStatus;
import com.forerp.erp.outbound.dto.OutboundCreateRequest;
import com.forerp.erp.outbound.dto.OutboundListResponse;
import com.forerp.erp.outbound.repository.OutboundRepository;
import com.forerp.erp.outbound.service.support.OutboundBuilder;
import com.forerp.erp.outbound.service.support.OutboundLoader;
import com.forerp.erp.shipment.domain.Shipment;
import com.forerp.erp.shipment.domain.ShipmentStatus;
import com.forerp.erp.storeproduct.domain.StoreProduct;
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
public class OutboundService {

    private final OutboundRepository outboundRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;

    private final OutboundLoader loader;
    private final OutboundBuilder builder;

    /* 출고 생성 */
    public Outbound createOutbound(OutboundCreateRequest req) {
        Outbound outbound = builder.buildOutboundAggregate(req);

        outbound.getOrder().markPrepared();

        return outboundRepository.save(outbound);
    }

    /* 배송 출발 + 재고 차감 + 출고 확정 */
    public Outbound confirmOutbound(Long outboundId, User actor, String carrier, String trackingNumber) {
        Outbound outbound = loader.loadOutbound(outboundId);
        Shipment shipment = loader.requireShipment(outbound);

        shipment.depart(carrier, trackingNumber);

        outbound.getItems().forEach(item -> {
            InventoryHistory history = item.ship(actor);
            inventoryHistoryRepository.save(history);
        });

        outbound.confirm();
        outbound.getOrder().markShipped();

        return outbound;
    }

    /* 배송 도착 + 주문 도착 + 출고 ARRIVED */
    public Outbound arriveOutbound(Long outboundId) {
        Outbound outbound = loader.loadOutbound(outboundId);
        Shipment shipment = loader.requireShipment(outbound);
        loader.requireOutboundStatus(outbound, outbound.getStatus());

        // 배송 도착 (SHIPPING -> ARRIVED)
        shipment.arrive();
        // 주문 도착 (SHIPPED -> ARRIVED)
        outbound.getOrder().markArrived();
        // 출고 도착 (CONFIRMED -> ARRIVED)
        outbound.arrive();

        return outbound;
    }

    /* 출고 취소 (출발 전까지만) */
    public Outbound cancelOutbound(Long outboundId) {
        Outbound outbound = loader.loadOutbound(outboundId);

        Shipment shipment = outbound.getShipment();
        if (shipment != null && shipment.getStatus() != ShipmentStatus.READY) {
            throw new IllegalStateException("배송 출발 이후에는 출고 취소가 불가능합니다.");
        }

        outbound.cancel();
        return outbound;
    }

    /* 출고 단건 조회 */
    @Transactional(readOnly = true)
    public Outbound getOutbound(Long outboundId) {
        return loader.loadOutbound(outboundId);
    }

    /* 출고 목록 조회 */
    @Transactional(readOnly = true)
    public OutboundListResponse listOutbounds(
            Long storeId,
            Long warehouseId,
            String status,
            String from,
            String to,
            int page,
            int size
    ) {
        OutboundStatus st = QueryParamParser.parseEnumOrNull(status, OutboundStatus.class, "status");
        LocalDateTime fromDt = QueryParamParser.parseFromDate(from);
        LocalDateTime toDt = QueryParamParser.parseToDateExclusive(to);

        PageRequest pageable = PageRequest.of(page, size);
        Page<Outbound> result = outboundRepository.search(storeId, st, warehouseId, fromDt, toDt, pageable);

        List<OutboundListResponse.OutboundListItem> content = result.getContent().stream()
                .map(o -> new OutboundListResponse.OutboundListItem(
                        o.getId(),
                        o.getOrder().getId(),
                        o.getStore().getId(),
                        extractWarehouseId(o),
                        o.getStatus().name(),
                        o.getCreatedAt(),
                        o.getShipment() == null ? null : o.getShipment().getStatus().name()
                ))
                .toList();

        return new OutboundListResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private Long extractWarehouseId(Outbound o) {
        if (o.getItems() == null || o.getItems().isEmpty()) return null;
        StoreProduct sp = o.getItems().get(0).getStoreProduct();
        if (sp == null || sp.getWarehouse() == null) return null;
        return sp.getWarehouse().getId();
    }
}