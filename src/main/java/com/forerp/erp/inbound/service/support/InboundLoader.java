package com.forerp.erp.inbound.service.support;

import com.forerp.erp.inbound.domain.Inbound;
import com.forerp.erp.inbound.repository.InboundRepository;
import com.forerp.erp.product.domain.Product;
import com.forerp.erp.product.repository.ProductRepository;
import com.forerp.erp.purchase_order.domain.PurchaseOrder;
import com.forerp.erp.purchase_order.repository.PurchaseOrderRepository;
import com.forerp.erp.shipment.domain.Shipment;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.store.repository.StoreRepository;
import com.forerp.erp.storeproduct.domain.StoreProduct;
import com.forerp.erp.storeproduct.repository.StoreProductRepository;
import com.forerp.erp.warehouse.domain.Warehouse;
import com.forerp.erp.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InboundLoader {

    private final InboundRepository inboundRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StoreRepository storeRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final StoreProductRepository storeProductRepository;

    public PurchaseOrder loadPurchaseOrder(Long purchaseOrderId) {
        return purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new IllegalArgumentException("발주를 찾을 수 없습니다."));
    }

    public Store loadStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));
    }

    public Warehouse loadWarehouse(Long warehouseId) {
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("창고를 찾을 수 없습니다."));
    }

    public Product loadProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. productId=" + productId));
    }

    /* StoreProduct가 없으면 생성 */
    public StoreProduct loadOrCreateStoreProduct(Store store, Warehouse warehouse, Product product) {
        return storeProductRepository
                .findByStore_IdAndWarehouse_IdAndProduct_Id(store.getId(), warehouse.getId(), product.getId())
                .orElseGet(() -> storeProductRepository.save(StoreProduct.create(store, warehouse, product)));
    }

    public Inbound loadInbound(Long inboundId) {
        return inboundRepository.findById(inboundId)
                .orElseThrow(() -> new IllegalArgumentException("입고를 찾을 수 없습니다."));
    }

    public Shipment requireShipment(Inbound inbound) {
        Shipment shipment = inbound.getShipment();
        if (shipment == null) {
            throw new IllegalStateException("입고에 연결된 배송 정보가 없습니다.");
        }
        return shipment;
    }
}