package com.forerp.erp.discard.service.support;

import com.forerp.erp.discard.domain.Discard;
import com.forerp.erp.discard.domain.DiscardItem;
import com.forerp.erp.discard.dto.DiscardCreateRequest;
import com.forerp.erp.product.domain.Product;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.storeproduct.domain.StoreProduct;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.warehouse.domain.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DiscardBuilder {

    private final DiscardLoader loader;

    public Discard buildDiscardAggregate(DiscardCreateRequest req, User actor) {
        Store store = loader.loadStore(req.getStoreId());
        Warehouse warehouse = loader.loadWarehouse(req.getWarehouseId());

        List<DiscardItem> items = req.getItems().stream()
                .map(i -> {
                    Product product = loader.loadProduct(i.getProductId());
                    StoreProduct sp = loader.loadStoreProduct(store, warehouse, product);
                    return DiscardItem.create(sp, i.getQty());
                })
                .toList();

        return Discard.create(store, warehouse, req.getReason(), actor, items);
    }
}