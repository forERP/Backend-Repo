package com.forerp.erp.inbound.service.support;

import com.forerp.erp.inbound.domain.Inbound;
import com.forerp.erp.inbound.domain.InboundItem;
import com.forerp.erp.inbound.dto.InboundCreateRequest;
import com.forerp.erp.product.domain.Product;
import com.forerp.erp.purchase_order.domain.PurchaseOrder;
import com.forerp.erp.shipment.domain.Shipment;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.storeproduct.domain.StoreProduct;
import com.forerp.erp.warehouse.domain.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InboundBuilder {

    private final InboundLoader loader;

    public Inbound buildInboundAggregate(InboundCreateRequest req) {
        PurchaseOrder po = loader.loadPurchaseOrder(req.getPurchaseOrderId());
        Store store = loader.loadStore(req.getStoreId());
        Warehouse warehouse = loader.loadWarehouse(req.getWarehouseId());

        List<InboundItem> items = req.getItems().stream()
                .map(i -> buildInboundItem(store, warehouse, i))
                .toList();

        Inbound inbound = Inbound.create(po, store, warehouse, items);
        Shipment.createForInbound(inbound);
        return inbound;
    }

    private InboundItem buildInboundItem(
            Store store,
            Warehouse warehouse,
            InboundCreateRequest.InboundCreateItem i
    ) {
        Product product = loader.loadProduct(i.getProductId());
        StoreProduct sp = loader.loadOrCreateStoreProduct(store, warehouse, product);
        return InboundItem.create(product, sp, i.getQty());
    }
}