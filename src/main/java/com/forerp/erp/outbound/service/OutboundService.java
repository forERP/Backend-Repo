package com.forerp.erp.outbound.service;

import com.forerp.erp.inventory.domain.InventoryHistory;
import com.forerp.erp.inventory.repository.InventoryHistoryRepository;
import com.forerp.erp.order.domain.Order;
import com.forerp.erp.order.domain.OrderItem;
import com.forerp.erp.order.repository.OrderItemRepository;
import com.forerp.erp.order.repository.OrderRepository;
import com.forerp.erp.outbound.domain.Outbound;
import com.forerp.erp.outbound.domain.OutboundItem;
import com.forerp.erp.outbound.domain.OutboundStatus;
import com.forerp.erp.outbound.dto.OutboundCreateRequest;
import com.forerp.erp.outbound.dto.OutboundListResponse;
import com.forerp.erp.outbound.repository.OutboundRepository;
import com.forerp.erp.product.domain.Product;
import com.forerp.erp.product.repository.ProductRepository;
import com.forerp.erp.shipment.domain.Shipment;
import com.forerp.erp.shipment.domain.ShipmentStatus;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.store.repository.StoreRepository;
import com.forerp.erp.storeproduct.domain.StoreProduct;
import com.forerp.erp.storeproduct.repository.StoreProductRepository;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.warehouse.domain.Warehouse;
import com.forerp.erp.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OutboundService {

    private final OutboundRepository outboundRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final WarehouseRepository warehouseRepository;
    private final OrderItemRepository orderItemRepository;
    private final StoreProductRepository storeProductRepository;

    /* ===== 출고 생성 ===== */
    @Transactional
    public Outbound createOutbound(OutboundCreateRequest req) {
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        Store store = storeRepository.findById(req.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));

        Warehouse warehouse = warehouseRepository.findById(req.getWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException("창고를 찾을 수 없습니다."));

        List<OutboundItem> items = req.getItems().stream()
                .map(i -> {
                    OrderItem orderItem = orderItemRepository.findById(i.getOrderItemId())
                            .orElseThrow(() -> new IllegalArgumentException("주문 아이템을 찾을 수 없습니다. orderItemId=" + i.getOrderItemId()));

                    if (!orderItem.getOrder().getId().equals(order.getId())) {
                        throw new IllegalArgumentException("주문에 속하지 않은 orderItem 입니다. orderItemId=" + i.getOrderItemId());
                    }

                    Product product = orderItem.getProduct();

                    StoreProduct sp = storeProductRepository
                            .findByStore_IdAndWarehouse_IdAndProduct_Id(store.getId(), warehouse.getId(), product.getId())
                            .orElseThrow(() -> new IllegalStateException("해당 창고에 재고 정보(StoreProduct)가 없습니다. productId=" + product.getId()));

                    return OutboundItem.create(orderItem, sp, i.getQty());
                })
                .toList();

        Outbound outbound = Outbound.create(order, store, items);
        Shipment.createForOutbound(outbound);
        return outboundRepository.save(outbound);
    }

    /* ===== 배송 출발 + 재고 차감 + 출고 확정 ===== */
    public Outbound confirmOutbound(Long outboundId, User actor, String carrier, String trackingNumber) {
        Outbound outbound = outboundRepository.findById(outboundId)
                .orElseThrow(() -> new IllegalArgumentException("출고를 찾을 수 없습니다."));

        Shipment shipment = outbound.getShipment();
        if (shipment == null) throw new IllegalStateException("출고에 연결된 배송 정보가 없습니다.");

        shipment.depart(carrier, trackingNumber);

        outbound.getItems().forEach(item -> {
            InventoryHistory history = item.ship(actor);
            inventoryHistoryRepository.save(history);
        });

        outbound.confirm();
        return outbound;
    }

    /* ===== 출고 취소 (출발 전까지만) ===== */
    public Outbound cancelOutbound(Long outboundId) {
        Outbound outbound = outboundRepository.findById(outboundId)
                .orElseThrow(() -> new IllegalArgumentException("출고를 찾을 수 없습니다."));

        Shipment shipment = outbound.getShipment();
        if (shipment != null && shipment.getStatus() != ShipmentStatus.READY) {
            throw new IllegalStateException("배송 출발 이후에는 출고 취소가 불가능합니다.");
        }

        outbound.cancel();
        return outbound;
    }

    /* ===== 출고 조회 ===== */
    @Transactional(readOnly = true)
    public Outbound getOutbound(Long outboundId) {
        return outboundRepository.findById(outboundId)
                .orElseThrow(() -> new IllegalArgumentException("출고를 찾을 수 없습니다."));
    }

    /* ===== 출고 목록 조회 ===== */
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
        OutboundStatus st = parseOutboundStatus(status);
        LocalDateTime fromDt = parseFromDate(from);
        LocalDateTime toDt = parseToDateExclusive(to);

        PageRequest pageable = PageRequest.of(page, size);
        Page<Outbound> result = outboundRepository.search(storeId, st, warehouseId, fromDt, toDt, pageable);

        List<OutboundListResponse.OutboundListItem> content = result.getContent().stream()
                .map(o -> {
                    Long whId = extractWarehouseId(o); // 아래 helper
                    return new OutboundListResponse.OutboundListItem(
                            o.getId(),
                            o.getOrder().getId(),
                            o.getStore().getId(),
                            whId,
                            o.getStatus().name(),
                            o.getCreatedAt(),
                            o.getShipment() == null ? null : o.getShipment().getStatus().name()
                    );
                })
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
        // 현재 create 시 warehouseId가 단일로 고정되므로 items 중 아무거나의 warehouseId를 대표로 사용
        if (o.getItems() == null || o.getItems().isEmpty()) return null;
        StoreProduct sp = o.getItems().get(0).getStoreProduct();
        if (sp == null || sp.getWarehouse() == null) return null;
        return sp.getWarehouse().getId();
    }

    private OutboundStatus parseOutboundStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return OutboundStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 status 입니다: " + status
                    + " (허용: CREATED, CONFIRMED, CANCELED)");
        }
    }

    private LocalDateTime parseFromDate(String from) {
        if (from == null || from.isBlank()) return null;
        try {
            return LocalDate.parse(from.trim()).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("from 형식이 올바르지 않습니다. yyyy-MM-dd");
        }
    }

    private LocalDateTime parseToDateExclusive(String to) {
        if (to == null || to.isBlank()) return null;
        try {
            return LocalDate.parse(to.trim()).plusDays(1).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("to 형식이 올바르지 않습니다. yyyy-MM-dd");
        }
    }
}