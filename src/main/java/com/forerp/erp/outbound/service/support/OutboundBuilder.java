package com.forerp.erp.outbound.service.support;

import com.forerp.erp.order.domain.Order;
import com.forerp.erp.order.domain.OrderItem;
import com.forerp.erp.order.domain.OrderItemComponent;
import com.forerp.erp.outbound.domain.Outbound;
import com.forerp.erp.outbound.domain.OutboundItem;
import com.forerp.erp.outbound.dto.OutboundCreateRequest;
import com.forerp.erp.product.domain.Product;
import com.forerp.erp.shipment.domain.Shipment;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.storeproduct.domain.StoreProduct;
import com.forerp.erp.warehouse.domain.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboundBuilder {

    private final OutboundLoader loader;

    public Outbound buildOutboundAggregate(OutboundCreateRequest req) {
        Order order = loader.loadOrder(req.getOrderId());
        Store store = loader.loadStore(req.getStoreId());
        Warehouse warehouse = loader.loadWarehouse(req.getWarehouseId());

        List<OutboundItem> items = req.getItems().stream()
                .flatMap(i -> buildOutboundItems(i, order, store, warehouse).stream())
                .toList();

        Outbound outbound = Outbound.create(order, store, items);
        Shipment.createForOutbound(outbound);
        return outbound;
    }

    private List<OutboundItem> buildOutboundItems(
            OutboundCreateRequest.OutboundCreateItem i,
            Order order,
            Store store,
            Warehouse warehouse
    ) {
        OrderItem orderItem = loader.loadOrderItem(i.getOrderItemId());
        loader.validateOrderItemBelongsToOrder(orderItem, order);

        if (i.getQty() > orderItem.getQuantity()) {
            throw new IllegalArgumentException("출고 수량이 주문 수량을 초과했습니다. orderItemId=" + orderItem.getId());
        }

        List<OrderItemComponent> components = loader.loadOrderItemComponents(orderItem.getId());
        if (components == null || components.isEmpty()) {
            Product product = orderItem.getProduct();
            StoreProduct sp = loader.loadStoreProduct(store, warehouse, product);
            return List.of(OutboundItem.create(orderItem, sp, i.getQty()));
        }

        List<OutboundItem> outboundItems = new ArrayList<>();
        for (OrderItemComponent component : components) {
            Product componentProduct = component.getComponentProduct();
            StoreProduct sp = loader.loadStoreProduct(store, warehouse, componentProduct);
            int qty = i.getQty() * component.getQuantityPerOrderItem();
            outboundItems.add(OutboundItem.create(orderItem, sp, qty));
        }
        return outboundItems;
    }
}
