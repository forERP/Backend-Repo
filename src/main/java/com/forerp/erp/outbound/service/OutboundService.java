package com.forerp.erp.outbound.service;

import com.forerp.erp.auditlog.AuditLogAction;
import com.forerp.erp.auditlog.AuditLogService;
import com.forerp.erp.auditlog.AuditLogTargetType;
import com.forerp.erp.common.query.QueryParamParser;
import com.forerp.erp.inventory.domain.InventoryHistory;
import com.forerp.erp.inventory.repository.InventoryHistoryRepository;
import com.forerp.erp.order.domain.OrderStatus;
import com.forerp.erp.outbound.domain.Outbound;
import com.forerp.erp.outbound.domain.OutboundStatus;
import com.forerp.erp.outbound.dto.OutboundCreateRequest;
import com.forerp.erp.outbound.dto.OutboundListResponse;
import com.forerp.erp.outbound.repository.OutboundRepository;
import com.forerp.erp.outbound.service.support.OutboundBuilder;
import com.forerp.erp.outbound.service.support.OutboundLoader;
import com.forerp.erp.realtime.service.RealtimeEventService;
import com.forerp.erp.shipment.domain.Shipment;
import com.forerp.erp.shipment.domain.ShipmentStatus;
import com.forerp.erp.shipment.service.ShipmentService;
import com.forerp.erp.storeproduct.domain.StoreProduct;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.warehouse.domain.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class OutboundService {

    private final OutboundRepository outboundRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;
    private final RealtimeEventService realtimeEventService;
    private final ShipmentService shipmentService;
    private final AuditLogService auditLogService;

    private final OutboundLoader loader;
    private final OutboundBuilder builder;

    public Outbound createOutbound(OutboundCreateRequest req) {
        Outbound outbound = builder.buildOutboundAggregate(req);
        outbound.getOrder().markPrepared();

        Outbound saved = outboundRepository.save(outbound);
        realtimeEventService.publishOrderChanged(saved.getStore().getId(), saved.getOrder().getId(), "outbound_created");
        return saved;
    }

    public Outbound confirmOutbound(Long outboundId, User actor, String carrierCode, String carrier, String trackingNumber) {
        Outbound outbound = loader.loadOutbound(outboundId);
        Shipment shipment = loader.requireShipment(outbound);

        shipment.depart(carrierCode, carrier, trackingNumber);
        shipmentService.registerWebhookIfPossible(shipment);
        realtimeEventService.publish("shipment.changed", outbound.getStore().getId(), Map.of(
                "shipmentId", shipment.getId(),
                "flowType", "OUTBOUND",
                "reason", "outbound_confirmed"
        ));

        outbound.getItems().forEach(item -> {
            InventoryHistory history = item.ship(actor);
            inventoryHistoryRepository.save(history);
        });

        outbound.confirm();
        outbound.getOrder().markShipped();
        realtimeEventService.publishInventoryChanged(outbound.getStore().getId(), "outbound_confirmed");
        realtimeEventService.publishOrderChanged(outbound.getStore().getId(), outbound.getOrder().getId(), "order_shipped");
        auditLogService.logActionSafely(actor, AuditLogAction.OUTBOUND_CONFIRM, AuditLogTargetType.OUTBOUND, outbound.getId());

        return outbound;
    }

    public Outbound arriveOutbound(Long outboundId) {
        Outbound outbound = loader.loadOutbound(outboundId);
        Shipment shipment = loader.requireShipment(outbound);

        if (outbound.getStatus() != OutboundStatus.CONFIRMED && outbound.getStatus() != OutboundStatus.ARRIVED) {
            throw new IllegalStateException("출고 상태에서는 배송 완료 처리를 할 수 없습니다.");
        }

        if (shipment.getStatus() == ShipmentStatus.SHIPPING) {
            shipment.arrive();
        } else if (shipment.getStatus() != ShipmentStatus.ARRIVED) {
            throw new IllegalStateException("현재 배송 상태에서는 배송 완료 처리를 할 수 없습니다.");
        }

        if (outbound.getOrder().getStatus() != OrderStatus.ARRIVED) {
            outbound.getOrder().markArrived();
        }
        if (outbound.getStatus() == OutboundStatus.CONFIRMED) {
            outbound.arrive();
        }

        realtimeEventService.publishOrderChanged(outbound.getStore().getId(), outbound.getOrder().getId(), "order_arrived");
        realtimeEventService.publish("shipment.changed", outbound.getStore().getId(), Map.of(
                "shipmentId", shipment.getId(),
                "flowType", "OUTBOUND",
                "reason", "outbound_arrived"
        ));
        return outbound;
    }

    public Outbound cancelOutbound(Long outboundId) {
        Outbound outbound = loader.loadOutbound(outboundId);

        Shipment shipment = outbound.getShipment();
        if (shipment != null && shipment.getStatus() != ShipmentStatus.READY) {
            throw new IllegalStateException("배송 출발 이후에는 출고를 취소할 수 없습니다.");
        }

        outbound.cancel();
        realtimeEventService.publishOrderChanged(outbound.getStore().getId(), outbound.getOrder().getId(), "outbound_canceled");
        auditLogService.logCurrentUserAction(AuditLogAction.OUTBOUND_CANCEL, AuditLogTargetType.OUTBOUND, outbound.getId());
        return outbound;
    }

    @Transactional(readOnly = true)
    public Outbound getOutbound(Long outboundId) {
        return loader.loadOutbound(outboundId);
    }

    @Transactional(readOnly = true)
    public OutboundListResponse listOutbounds(
            Long storeId,
            String storeKeyword,
            String storeName,
            String storeCode,
            Long warehouseId,
            String status,
            String shipmentStatus,
            String from,
            String to,
            int page,
            int size
    ) {
        OutboundStatus st = QueryParamParser.parseEnumOrNull(status, OutboundStatus.class, "status");
        ShipmentStatus shSt = QueryParamParser.parseEnumOrNull(shipmentStatus, ShipmentStatus.class, "shipmentStatus");
        LocalDateTime fromDt = QueryParamParser.parseFromDate(from);
        LocalDateTime toDt = QueryParamParser.parseToDateExclusive(to);

        PageRequest pageable = PageRequest.of(page, size);
        Page<Outbound> result = outboundRepository.search(
                storeId,
                normalize(storeKeyword),
                normalize(storeName),
                normalize(storeCode),
                st,
                shSt,
                warehouseId,
                fromDt,
                toDt,
                pageable
        );

        List<OutboundListResponse.OutboundListItem> content = result.getContent().stream()
                .map(o -> new OutboundListResponse.OutboundListItem(
                        o.getId(),
                        o.getOrder().getId(),
                        o.getStore().getId(),
                        o.getStore().getName(),
                        o.getStore().getStoreCode(),
                        extractWarehouseId(o),
                        extractWarehouseCode(o),
                        extractWarehouseName(o),
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

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long extractWarehouseId(Outbound outbound) {
        Warehouse warehouse = extractWarehouse(outbound);
        return warehouse == null ? null : warehouse.getId();
    }

    private String extractWarehouseCode(Outbound outbound) {
        Warehouse warehouse = extractWarehouse(outbound);
        return warehouse == null ? null : warehouse.getCode();
    }

    private String extractWarehouseName(Outbound outbound) {
        Warehouse warehouse = extractWarehouse(outbound);
        return warehouse == null ? null : warehouse.getName();
    }

    private Warehouse extractWarehouse(Outbound outbound) {
        if (outbound.getItems() == null || outbound.getItems().isEmpty()) {
            return null;
        }
        StoreProduct storeProduct = outbound.getItems().get(0).getStoreProduct();
        if (storeProduct == null || storeProduct.getWarehouse() == null) {
            return null;
        }
        return storeProduct.getWarehouse();
    }
}
