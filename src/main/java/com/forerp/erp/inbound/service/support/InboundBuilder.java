package com.forerp.erp.inbound.service.support;

import com.forerp.erp.inbound.domain.Inbound;
import com.forerp.erp.inbound.domain.InboundItem;
import com.forerp.erp.inbound.dto.InboundCreateRequest;
import com.forerp.erp.product.domain.Product;
import com.forerp.erp.purchase_order.domain.PurchaseOrder;
import com.forerp.erp.purchase_order.domain.PurchaseOrderItem;
import com.forerp.erp.purchase_order.domain.PurchaseOrderStatus;
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
        PurchaseOrder po = loader.loadPurchaseOrderForInboundCreate(req.getPurchaseOrderId());

        if (po.getStatus() != PurchaseOrderStatus.ORDERED) {
            throw new IllegalStateException("ORDERED 상태의 발주만 입고를 생성할 수 있습니다.");
        }

        Store store = po.getStore();
        Warehouse warehouse = po.getWarehouse();

        List<InboundItem> items = po.getItems().stream()
                .map(oi -> buildInboundItem(store, warehouse, oi))
                .toList();

        Inbound inbound = Inbound.create(po, store, warehouse, items);
        Shipment.createForInbound(inbound);
        return inbound;
    }

    private InboundItem buildInboundItem(Store store, Warehouse warehouse, PurchaseOrderItem oi) {
        Product product = oi.getProduct();
        StoreProduct sp = loader.loadOrCreateStoreProduct(store, warehouse, product);
        return InboundItem.create(product, sp, oi.getQuantity());
    }
}