package com.forerp.erp.storeproduct.service;

import com.forerp.erp.product.domain.Product;
import com.forerp.erp.product.domain.ProductStatus;
import com.forerp.erp.product.repository.ProductRepository;
import com.forerp.erp.storeproduct.domain.StoreProduct;
import com.forerp.erp.storeproduct.repository.StoreProductRepository;
import com.forerp.erp.warehouse.domain.Warehouse;
import com.forerp.erp.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StoreProductSyncService {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StoreProductRepository storeProductRepository;

    @Transactional
    public void syncActiveProductToActiveWarehouses(Product product) {
        if (product == null || product.getId() == null || product.getStatus() != ProductStatus.ACTIVE) {
            return;
        }

        List<Warehouse> targetWarehouses = warehouseRepository.findByActiveTrue();
        if (targetWarehouses.isEmpty()) {
            return;
        }

        for (Warehouse warehouse : targetWarehouses) {
            syncWarehouseProducts(warehouse, List.of(product));
        }
    }

    @Transactional
    public void syncActiveProductsToWarehouse(Warehouse warehouse) {
        if (warehouse == null || warehouse.getId() == null || !warehouse.isActive()) {
            return;
        }

        List<Product> activeProducts = productRepository.findAllByStatus(ProductStatus.ACTIVE);
        if (activeProducts.isEmpty()) {
            return;
        }

        syncWarehouseProducts(warehouse, activeProducts);
    }

    @Transactional
    public void syncActiveProductsForStore(Long storeId, Long warehouseId) {
        List<Warehouse> targetWarehouses = resolveTargetWarehouses(storeId, warehouseId);
        if (targetWarehouses.isEmpty()) {
            return;
        }

        List<Product> activeProducts = productRepository.findAllByStatus(ProductStatus.ACTIVE);
        if (activeProducts.isEmpty()) {
            return;
        }

        for (Warehouse warehouse : targetWarehouses) {
            syncWarehouseProducts(warehouse, activeProducts);
        }
    }

    private List<Warehouse> resolveTargetWarehouses(Long storeId, Long warehouseId) {
        if (warehouseId != null) {
            Warehouse warehouse = warehouseRepository.findById(warehouseId)
                    .orElseThrow(() -> new IllegalArgumentException("창고를 찾을 수 없습니다. warehouseId=" + warehouseId));

            if (storeId != null && !warehouse.getStore().getId().equals(storeId)) {
                throw new IllegalArgumentException("요청한 매장과 창고가 일치하지 않습니다.");
            }

            if (!warehouse.isActive()) {
                return List.of();
            }

            return List.of(warehouse);
        }

        if (storeId != null) {
            return warehouseRepository.findByStore_IdAndActiveTrue(storeId);
        }

        return warehouseRepository.findByActiveTrue();
    }

    private void syncWarehouseProducts(Warehouse warehouse, List<Product> activeProducts) {
        List<Long> productIds = activeProducts.stream()
                .map(Product::getId)
                .toList();

        if (productIds.isEmpty()) {
            return;
        }

        Set<Long> existingProductIds = storeProductRepository.findExistingProductIds(
                warehouse.getStore().getId(),
                warehouse.getId(),
                productIds
        );

        Map<Long, StoreProduct> existingStoreProductByProductId = new HashMap<>();
        if (!existingProductIds.isEmpty()) {
            List<StoreProduct> existingStoreProducts = storeProductRepository.findByStore_IdAndWarehouse_IdAndProduct_IdIn(
                    warehouse.getStore().getId(),
                    warehouse.getId(),
                    productIds
            );
            for (StoreProduct existing : existingStoreProducts) {
                existingStoreProductByProductId.put(existing.getProduct().getId(), existing);
            }
        }

        List<StoreProduct> toCreate = new ArrayList<>();
        for (Product product : activeProducts) {
            StoreProduct existing = existingStoreProductByProductId.get(product.getId());
            if (existing != null) {
                existing.applyDefaultSalePriceIfUnset(product.getMsrpPrice());
                continue;
            }

            toCreate.add(StoreProduct.create(warehouse.getStore(), warehouse, product));
        }

        if (!toCreate.isEmpty()) {
            storeProductRepository.saveAll(toCreate);
        }
    }
}
