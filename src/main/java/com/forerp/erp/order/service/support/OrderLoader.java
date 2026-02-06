package com.forerp.erp.order.service.support;

import com.forerp.erp.order.domain.Order;
import com.forerp.erp.order.repository.OrderRepository;
import com.forerp.erp.product.domain.Product;
import com.forerp.erp.product.repository.ProductRepository;
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
public class OrderLoader {

    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final StoreProductRepository storeProductRepository;

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

    public StoreProduct loadStoreProduct(Long storeId, Long warehouseId, Long productId) {
        return storeProductRepository
                .findByStore_IdAndWarehouse_IdAndProduct_Id(storeId, warehouseId, productId)
                .orElseThrow(() -> new IllegalStateException(
                        "해당 창고에 재고 정보(StoreProduct)가 없습니다. productId=" + productId
                ));
    }

    public Order loadOrderDetail(Long orderId) {
        return orderRepository.findDetailById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
    }
}