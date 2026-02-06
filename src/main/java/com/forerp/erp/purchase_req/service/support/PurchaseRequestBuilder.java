package com.forerp.erp.purchase_req.service.support;

import com.forerp.erp.product.domain.Product;
import com.forerp.erp.purchase_req.domain.PurchaseRequest;
import com.forerp.erp.purchase_req.domain.PurchaseRequestItem;
import com.forerp.erp.purchase_req.dto.PurchaseRequestCreateRequest;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PurchaseRequestBuilder {

    private final PurchaseRequestLoader loader;

    public PurchaseRequest buildCreateAggregate(PurchaseRequestCreateRequest req, User actor) {
        Store store = loader.loadStore(req.getStoreId());

        List<PurchaseRequestItem> items = req.getItems().stream()
                .map(i -> {
                    Product p = loader.loadProduct(i.getProductId());
                    return PurchaseRequestItem.create(p, i.getQty());
                })
                .toList();

        return PurchaseRequest.create(store, actor, req.getMemo(), items);
    }
}