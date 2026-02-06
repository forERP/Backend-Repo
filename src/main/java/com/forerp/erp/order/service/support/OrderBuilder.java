package com.forerp.erp.order.service.support;

import com.forerp.erp.order.domain.Order;
import com.forerp.erp.order.domain.OrderItem;
import com.forerp.erp.order.dto.OrderCreateRequest;
import com.forerp.erp.product.domain.Product;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.storeproduct.domain.StoreProduct;
import com.forerp.erp.warehouse.domain.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderBuilder {

    private final OrderLoader loader;

    public Order buildOrderAggregate(OrderCreateRequest req) {
        Store store = loader.loadStore(req.getStoreId());
        Warehouse warehouse = loader.loadWarehouse(req.getWarehouseId()); // 가격/재고 기준 창고

        List<OrderItem> items = req.getItems().stream()
                .map(i -> buildOrderItem(store.getId(), warehouse.getId(), i))
                .toList();

        return Order.create(store, items);
    }

    private OrderItem buildOrderItem(Long storeId, Long warehouseId, OrderCreateRequest.OrderCreateItem i) {
        Product product = loader.loadProduct(i.getProductId());
        StoreProduct sp = loader.loadStoreProduct(storeId, warehouseId, product.getId());

        // unitPrice는 주문 시점 스냅샷 (StoreProduct.salePrice)
        return OrderItem.create(product, i.getQty(), sp.getSalePrice());
    }
}