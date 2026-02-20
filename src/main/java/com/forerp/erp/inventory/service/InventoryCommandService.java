package com.forerp.erp.inventory.service;

import com.forerp.erp.inventory.dto.InventoryResponse;
import com.forerp.erp.inventory.dto.InventoryUpdateRequest;
import com.forerp.erp.product.domain.ProductStatus;
import com.forerp.erp.realtime.service.RealtimeEventService;
import com.forerp.erp.storeproduct.domain.SaleStatus;
import com.forerp.erp.storeproduct.domain.StoreProduct;
import com.forerp.erp.storeproduct.repository.StoreProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryCommandService {

    private final StoreProductRepository storeProductRepository;
    private final InventoryQueryService inventoryQueryService;
    private final RealtimeEventService realtimeEventService;

    @Transactional
    public InventoryResponse updateSaleStatus(Long storeProductId, SaleStatus saleStatus) {
        if (saleStatus == null) {
            throw new IllegalArgumentException("saleStatus is required.");
        }

        StoreProduct storeProduct = findActiveStoreProduct(storeProductId);
        storeProduct.changeSaleStatus(saleStatus);
        storeProductRepository.flush();
        realtimeEventService.publishInventoryChanged(storeProduct.getStore().getId(), "sale_status_updated");

        return inventoryQueryService.toResponse(storeProduct);
    }

    @Transactional
    public InventoryResponse update(Long storeProductId, InventoryUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request body is required.");
        }

        if (request.getSaleStatus() == null && request.getSalePrice() == null) {
            throw new IllegalArgumentException("At least one field must be provided: saleStatus or salePrice.");
        }

        StoreProduct storeProduct = findActiveStoreProduct(storeProductId);

        if (request.getSaleStatus() != null) {
            storeProduct.changeSaleStatus(request.getSaleStatus());
        }

        if (request.getSalePrice() != null) {
            storeProduct.changeSalePrice(request.getSalePrice());
        }

        storeProductRepository.flush();
        realtimeEventService.publishInventoryChanged(storeProduct.getStore().getId(), "inventory_metadata_updated");
        return inventoryQueryService.toResponse(storeProduct);
    }

    private StoreProduct findActiveStoreProduct(Long storeProductId) {
        StoreProduct storeProduct = storeProductRepository.findDetailById(storeProductId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Store product not found. storeProductId=" + storeProductId
                ));

        if (storeProduct.getProduct().getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalStateException("Sale fields cannot be changed for discontinued products.");
        }

        return storeProduct;
    }
}
