package com.forerp.erp.outbound.service.support;

import com.forerp.erp.order.domain.Order;
import com.forerp.erp.order.domain.OrderItem;
import com.forerp.erp.order.domain.OrderItemComponent;
import com.forerp.erp.order.repository.OrderItemComponentRepository;
import com.forerp.erp.order.repository.OrderItemRepository;
import com.forerp.erp.order.repository.OrderRepository;
import com.forerp.erp.outbound.domain.Outbound;
import com.forerp.erp.outbound.domain.OutboundStatus;
import com.forerp.erp.outbound.repository.OutboundRepository;
import com.forerp.erp.product.domain.Product;
import com.forerp.erp.shipment.domain.Shipment;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.store.repository.StoreRepository;
import com.forerp.erp.storeproduct.domain.StoreProduct;
import com.forerp.erp.storeproduct.repository.StoreProductRepository;
import com.forerp.erp.warehouse.domain.Warehouse;
import com.forerp.erp.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboundLoader {

    private final OutboundRepository outboundRepository;
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final WarehouseRepository warehouseRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemComponentRepository orderItemComponentRepository;
    private final StoreProductRepository storeProductRepository;

    public Order loadOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
    }

    public Store loadStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));
    }

    public Warehouse loadWarehouse(Long warehouseId) {
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("창고를 찾을 수 없습니다."));
    }

    public Outbound loadOutbound(Long outboundId) {
        return outboundRepository.findById(outboundId)
                .orElseThrow(() -> new IllegalArgumentException("출고를 찾을 수 없습니다."));
    }

    public OrderItem loadOrderItem(Long orderItemId) {
        return orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("주문 아이템을 찾을 수 없습니다. orderItemId=" + orderItemId));
    }

    public List<OrderItemComponent> loadOrderItemComponents(Long orderItemId) {
        return orderItemComponentRepository.findByOrderItem_IdIn(List.of(orderItemId));
    }

    public void validateOrderItemBelongsToOrder(OrderItem orderItem, Order order) {
        if (!orderItem.getOrder().getId().equals(order.getId())) {
            throw new IllegalArgumentException("주문에 속하지 않은 orderItem 입니다. orderItemId=" + orderItem.getId());
        }
    }

    public StoreProduct loadStoreProduct(Store store, Warehouse warehouse, Product product) {
        return storeProductRepository
                .findByStore_IdAndWarehouse_IdAndProduct_Id(store.getId(), warehouse.getId(), product.getId())
                .orElseThrow(() -> new IllegalStateException("해당 창고에 재고 정보(StoreProduct)가 없습니다. productId=" + product.getId()));
    }

    public Shipment requireShipment(Outbound outbound) {
        Shipment shipment = outbound.getShipment();
        if (shipment == null) {
            throw new IllegalStateException("출고에 연결된 배송 정보가 없습니다.");
        }
        return shipment;
    }

    public void requireOutboundStatus(Outbound outbound, OutboundStatus expected) {
        if (outbound.getStatus() != expected) {
            throw new IllegalStateException("출고 상태가 올바르지 않습니다. expected=" + expected);
        }
    }
}
