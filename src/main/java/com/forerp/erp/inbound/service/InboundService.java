package com.forerp.erp.inbound.service;

import com.forerp.erp.inbound.domain.Inbound;
import com.forerp.erp.inbound.domain.InboundItem;
import com.forerp.erp.inbound.domain.InboundStatus;
import com.forerp.erp.inbound.dto.InboundCreateRequest;
import com.forerp.erp.inbound.dto.InboundListResponse;
import com.forerp.erp.inbound.repository.InboundRepository;
import com.forerp.erp.inventory.domain.InventoryHistory;
import com.forerp.erp.inventory.repository.InventoryHistoryRepository;
import com.forerp.erp.product.domain.Product;
import com.forerp.erp.product.repository.ProductRepository;
import com.forerp.erp.purchase_order.domain.PurchaseOrder;
import com.forerp.erp.purchase_order.repository.PurchaseOrderRepository;
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
public class InboundService {

    private final InboundRepository inboundRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StoreRepository storeRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final StoreProductRepository storeProductRepository;

    /* ===== 입고 생성 (컨트롤러용: ID 기반) ===== */
    @Transactional
    public Inbound createInbound(InboundCreateRequest req) {
        PurchaseOrder po = purchaseOrderRepository.findById(req.getPurchaseOrderId())
                .orElseThrow(() -> new IllegalArgumentException("발주를 찾을 수 없습니다."));

        Store store = storeRepository.findById(req.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));

        Warehouse warehouse = warehouseRepository.findById(req.getWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException("창고를 찾을 수 없습니다."));

        List<InboundItem> items = req.getItems().stream()
                .map(i -> {
                    Product product = productRepository.findById(i.getProductId())
                            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. productId=" + i.getProductId()));

                    StoreProduct sp = storeProductRepository
                            .findByStore_IdAndWarehouse_IdAndProduct_Id(store.getId(), warehouse.getId(), product.getId())
                            .orElseGet(() -> storeProductRepository.save(StoreProduct.create(store, warehouse, product)));

                    return InboundItem.create(product, sp, i.getQty());
                })
                .toList();

        Inbound inbound = Inbound.create(po, store, warehouse, items);
        Shipment.createForInbound(inbound);
        return inboundRepository.save(inbound);
    }

    /* ===== 배송 출발 ===== */
    public Shipment departShipment(Long inboundId, String carrier, String trackingNumber) {
        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new IllegalArgumentException("입고를 찾을 수 없습니다."));

        Shipment shipment = inbound.getShipment();
        if (shipment == null) throw new IllegalStateException("입고에 연결된 배송 정보가 없습니다.");

        shipment.depart(carrier, trackingNumber);
        return shipment;
    }

    /* ===== 배송 도착 → 입고 확정 ===== */
    public Inbound confirmInbound(Long inboundId, User actor) {
        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new IllegalArgumentException("입고를 찾을 수 없습니다."));

        Shipment shipment = inbound.getShipment();
        if (shipment == null) throw new IllegalStateException("입고에 연결된 배송 정보가 없습니다.");

        shipment.arrive();

        inbound.getItems().forEach(item -> {
            InventoryHistory history = item.receive(actor);
            inventoryHistoryRepository.save(history);
        });

        inbound.confirm();

        PurchaseOrder po = inbound.getPurchaseOrder();
        po.markReceived();
        purchaseOrderRepository.save(po);

        return inbound;
    }

    /* ===== 입고 취소 (출발 전까지만) ===== */
    public Inbound cancelInbound(Long inboundId) {
        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new IllegalArgumentException("입고를 찾을 수 없습니다."));

        Shipment shipment = inbound.getShipment();
        if (shipment != null && shipment.getStatus() != ShipmentStatus.READY) {
            throw new IllegalStateException("배송 출발 이후에는 입고 취소가 불가능합니다.");
        }

        inbound.cancel();
        return inbound;
    }

    /* ===== 입고 조회 ===== */
    @Transactional(readOnly = true)
    public Inbound getInbound(Long inboundId) {
        return inboundRepository.findById(inboundId)
                .orElseThrow(() -> new IllegalArgumentException("입고를 찾을 수 없습니다."));
    }

    /* ===== 입고 목록 조회 ===== */
    @Transactional(readOnly = true)
    public InboundListResponse listInbounds(
            Long storeId,
            String status,
            String from, // yyyy-MM-dd
            String to,   // yyyy-MM-dd
            int page,
            int size
    ) {
        InboundStatus st = parseInboundStatus(status);
        LocalDateTime fromDt = parseFromDate(from);
        LocalDateTime toDt = parseToDateExclusive(to);

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

    private InboundStatus parseInboundStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return InboundStatus.valueOf(status.trim().toUpperCase());
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
            // to 날짜 포함을 위해 to+1일 00:00 미만으로 조회
            return LocalDate.parse(to.trim()).plusDays(1).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("to 형식이 올바르지 않습니다. yyyy-MM-dd");
        }
    }
}