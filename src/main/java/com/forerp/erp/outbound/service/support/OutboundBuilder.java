package com.forerp.erp.outbound.service.support;

import com.forerp.erp.order.domain.Order;
import com.forerp.erp.order.domain.OrderItem;
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
                .map(i -> buildOutboundItem(i, order, store, warehouse))
                .toList();

        Outbound outbound = Outbound.create(order, store, items);
        Shipment.createForOutbound(outbound);
        return outbound;
    }

    private OutboundItem buildOutboundItem(
            OutboundCreateRequest.OutboundCreateItem i,
            Order order,
            Store store,
            Warehouse warehouse
    ) {
        OrderItem orderItem = loader.loadOrderItem(i.getOrderItemId());
        loader.validateOrderItemBelongsToOrder(orderItem, order);

        Product product = orderItem.getProduct();
        StoreProduct sp = loader.loadStoreProduct(store, warehouse, product);

        return OutboundItem.create(orderItem, sp, i.getQty());
    }
}