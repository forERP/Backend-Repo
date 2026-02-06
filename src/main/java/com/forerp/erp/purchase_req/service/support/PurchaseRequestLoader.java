package com.forerp.erp.purchase_req.service.support;

import com.forerp.erp.product.domain.Product;
import com.forerp.erp.product.repository.ProductRepository;
import com.forerp.erp.purchase_req.domain.PurchaseRequest;
import com.forerp.erp.purchase_req.repository.PurchaseRequestRepository;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.store.repository.StoreRepository;
import com.forerp.erp.supplier.domain.Supplier;
import com.forerp.erp.supplier.repository.SupplierRepository;
import com.forerp.erp.warehouse.domain.Warehouse;
import com.forerp.erp.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PurchaseRequestLoader {

    private final PurchaseRequestRepository purchaseRequestRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;

    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;

    public PurchaseRequest loadPurchaseRequest(Long id) {
        return purchaseRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("발주요청을 찾을 수 없습니다."));
    }

    public Store loadStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));
    }

    public Product loadProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. productId=" + productId));
    }

    public Supplier loadSupplier(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("거래처를 찾을 수 없습니다."));
    }

    public Warehouse loadWarehouse(Long warehouseId) {
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("창고를 찾을 수 없습니다."));
    }
}