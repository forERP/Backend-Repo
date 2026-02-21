package com.forerp.erp.storeproduct.service;

import com.forerp.erp.product.domain.ProductBundle;
import com.forerp.erp.product.domain.ProductBundleItem;
import com.forerp.erp.product.domain.ProductStatus;
import com.forerp.erp.product.repository.ProductBundleRepository;
import com.forerp.erp.store.dto.StoreProductListResponseDto;
import com.forerp.erp.storeproduct.domain.StoreProduct;
import com.forerp.erp.storeproduct.repository.StoreProductRepository;
import com.forerp.erp.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreProductService {

    private final StoreProductRepository storeProductRepository;
    private final ProductBundleRepository productBundleRepository;
    private final WarehouseRepository warehouseRepository;

    public Page<StoreProductListResponseDto> getStoreProductList(Long storeId, Pageable pageable) {
        Page<StoreProductListResponseDto> basePage = storeProductRepository.findAllProductsWithStoreInfo(storeId, pageable);
        List<StoreProductListResponseDto> content = basePage.getContent();

        if (content.isEmpty()) {
            return basePage;
        }

        List<Long> productIds = content.stream()
                .map(StoreProductListResponseDto::getProductId)
                .toList();
        Map<Long, Integer> bundleQuantityByProductId = calculateBundleQuantityByProductId(storeId, productIds);

        if (bundleQuantityByProductId.isEmpty()) {
            return basePage;
        }

        List<StoreProductListResponseDto> adjusted = content.stream()
                .map(item -> {
                    Integer bundleQty = bundleQuantityByProductId.get(item.getProductId());
                    if (bundleQty == null) {
                        return item;
                    }
                    return cloneWithQuantity(item, bundleQty);
                })
                .toList();

        return new PageImpl<>(adjusted, pageable, basePage.getTotalElements());
    }

    private Map<Long, Integer> calculateBundleQuantityByProductId(Long storeId, List<Long> productIds) {
        List<ProductBundle> bundles = productBundleRepository.findByProduct_IdIn(productIds);
        if (bundles.isEmpty()) {
            return Map.of();
        }

        List<Long> warehouseIds = warehouseRepository.findByStore_IdAndActiveTrue(storeId).stream()
                .map(warehouse -> warehouse.getId())
                .toList();

        if (warehouseIds.isEmpty()) {
            Map<Long, Integer> emptyStock = new LinkedHashMap<>();
            bundles.forEach(bundle -> emptyStock.put(bundle.getProduct().getId(), 0));
            return emptyStock;
        }

        Set<Long> componentProductIds = bundles.stream()
                .flatMap(bundle -> bundle.getItems().stream())
                .map(ProductBundleItem::getComponentProduct)
                .map(componentProduct -> componentProduct.getId())
                .collect(Collectors.toSet());

        if (componentProductIds.isEmpty()) {
            Map<Long, Integer> emptyStock = new LinkedHashMap<>();
            bundles.forEach(bundle -> emptyStock.put(bundle.getProduct().getId(), 0));
            return emptyStock;
        }

        List<StoreProduct> stocks = storeProductRepository.findByStore_IdAndWarehouse_IdInAndProduct_IdInAndProduct_Status(
                storeId,
                warehouseIds,
                new ArrayList<>(componentProductIds),
                ProductStatus.ACTIVE
        );

        Map<Long, Map<Long, Integer>> qtyByWarehouseProduct = new HashMap<>();
        for (StoreProduct stock : stocks) {
            qtyByWarehouseProduct
                    .computeIfAbsent(stock.getWarehouse().getId(), key -> new HashMap<>())
                    .put(stock.getProduct().getId(), stock.getQuantity());
        }

        Map<Long, Integer> result = new LinkedHashMap<>();
        for (ProductBundle bundle : bundles) {
            int totalAvailable = 0;

            for (Long warehouseId : warehouseIds) {
                Map<Long, Integer> stockByProduct = qtyByWarehouseProduct.getOrDefault(warehouseId, Map.of());
                int availableByWarehouse = Integer.MAX_VALUE;

                for (ProductBundleItem item : bundle.getItems()) {
                    int stockQty = stockByProduct.getOrDefault(item.getComponentProduct().getId(), 0);
                    int availableByComponent = stockQty / item.getQuantityPerBundle();
                    availableByWarehouse = Math.min(availableByWarehouse, availableByComponent);
                }

                if (availableByWarehouse == Integer.MAX_VALUE) {
                    availableByWarehouse = 0;
                }
                totalAvailable += Math.max(availableByWarehouse, 0);
            }

            result.put(bundle.getProduct().getId(), totalAvailable);
        }

        return result;
    }

    private StoreProductListResponseDto cloneWithQuantity(StoreProductListResponseDto source, int quantity) {
        Long registeredMarker = source.isRegistered() ? 1L : null;
        return new StoreProductListResponseDto(
                source.getProductId(),
                source.getSku(),
                source.getName(),
                source.getCategoryName(),
                source.getCategoryImageUrl(),
                source.getImageUrl(),
                source.getMsrpPrice(),
                registeredMarker,
                quantity,
                source.getSaleStatus(),
                source.getSalePrice()
        );
    }
}
