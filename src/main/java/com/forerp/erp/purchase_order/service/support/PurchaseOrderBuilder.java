package com.forerp.erp.purchase_order.service.support;

import com.forerp.erp.purchase_order.domain.PurchaseOrder;
import com.forerp.erp.purchase_order.domain.PurchaseOrderItem;
import com.forerp.erp.purchase_req.domain.PurchaseRequest;
import com.forerp.erp.purchase_req.domain.PurchaseRequestItem;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.supplier.domain.Supplier;
import com.forerp.erp.warehouse.domain.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PurchaseOrderBuilder {

    public PurchaseOrder buildFromApprovedRequest(
            PurchaseRequest pr,
            Supplier supplier,
            Store store,
            Warehouse warehouse,
            String memo
    ) {
        // PurchaseRequestItem -> PurchaseOrderItem로 변환
        List<PurchaseOrderItem> items = pr.getItems().stream()
                .map(this::toOrderItem)
                .toList();

        return PurchaseOrder.createFromRequest(pr, supplier, store, warehouse, memo, items);
    }

    private PurchaseOrderItem toOrderItem(PurchaseRequestItem it) {
        return PurchaseOrderItem.create(it.getProduct(), it.getQuantity());
    }
}