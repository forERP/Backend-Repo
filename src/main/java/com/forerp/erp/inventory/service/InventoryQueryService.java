package com.forerp.erp.inventory.service;

import com.forerp.erp.inventory.dto.InventoryListResponse;
import com.forerp.erp.inventory.dto.InventoryResponse;
import com.forerp.erp.product.domain.ProductStatus;
import com.forerp.erp.storeproduct.domain.SaleStatus;
import com.forerp.erp.storeproduct.domain.StoreProduct;
import com.forerp.erp.storeproduct.repository.StoreProductRepository;
import com.forerp.erp.storeproduct.service.StoreProductSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryQueryService {

    private final StoreProductRepository storeProductRepository;
    private final StoreProductSyncService storeProductSyncService;

    public InventoryListResponse list(Long storeId, Long warehouseId, String keyword, int page, int size) {
        return list(storeId, warehouseId, null, null, keyword, null, page, size);
    }

    public InventoryListResponse list(
            Long storeId,
            Long warehouseId,
            String storeKeyword,
            String warehouseKeyword,
            String productKeyword,
            SaleStatus saleStatus,
            int page,
            int size
    ) {
        String normalizedStoreKeyword = normalizeKeyword(storeKeyword);
        String normalizedWarehouseKeyword = normalizeKeyword(warehouseKeyword);
        String normalizedProductKeyword = normalizeKeyword(productKeyword);
        PageRequest pageable = PageRequest.of(page, size);

        if (page == 0 && (storeId != null || warehouseId != null)) {
            storeProductSyncService.syncActiveProductsForStore(storeId, warehouseId);
        }

        Page<StoreProduct> result = storeProductRepository.searchInventory(
                storeId,
                warehouseId,
                normalizedStoreKeyword,
                normalizedWarehouseKeyword,
                normalizedProductKeyword,
                saleStatus,
                ProductStatus.ACTIVE,
                pageable
        );

        List<InventoryListResponse.InventoryItem> content = result.getContent().stream()
                .map(this::toListItem)
                .toList();

        return new InventoryListResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public InventoryResponse get(Long storeId, Long warehouseId, Long productId) {
        storeProductSyncService.syncActiveProductsForStore(storeId, warehouseId);

        StoreProduct storeProduct = (warehouseId == null)
                ? storeProductRepository.findFirstByStore_IdAndProduct_IdAndProduct_StatusOrderByUpdatedAtDesc(
                storeId, productId, ProductStatus.ACTIVE
        ).orElseThrow(() -> new IllegalArgumentException("해당 매장의 활성 상품 재고를 찾을 수 없습니다."))
                : storeProductRepository.findByStore_IdAndWarehouse_IdAndProduct_IdAndProduct_Status(
                storeId, warehouseId, productId, ProductStatus.ACTIVE
        ).orElseThrow(() -> new IllegalArgumentException("해당 창고의 활성 상품 재고를 찾을 수 없습니다."));

        return toResponse(storeProduct);
    }

    public InventoryResponse getByStoreProductId(Long storeProductId) {
        StoreProduct storeProduct = storeProductRepository.findDetailById(storeProductId)
                .orElseThrow(() -> new IllegalArgumentException("매장상품을 찾을 수 없습니다. storeProductId=" + storeProductId));
        return toResponse(storeProduct);
    }

    InventoryListResponse.InventoryItem toListItem(StoreProduct storeProduct) {
        return new InventoryListResponse.InventoryItem(
                storeProduct.getId(),
                storeProduct.getStore().getId(),
                storeProduct.getStore().getName(),
                storeProduct.getStore().getStoreCode(),
                storeProduct.getWarehouse().getId(),
                storeProduct.getWarehouse().getCode(),
                storeProduct.getWarehouse().getName(),
                storeProduct.getProduct().getId(),
                storeProduct.getProduct().getSku(),
                storeProduct.getProduct().getName(),
                storeProduct.getQuantity(),
                storeProduct.getSaleStatus(),
                storeProduct.getSalePrice(),
                storeProduct.getProduct().getMsrpPrice(),
                storeProduct.getUpdatedAt()
        );
    }

    InventoryResponse toResponse(StoreProduct storeProduct) {
        return new InventoryResponse(
                storeProduct.getId(),
                storeProduct.getStore().getId(),
                storeProduct.getStore().getName(),
                storeProduct.getStore().getStoreCode(),
                storeProduct.getWarehouse().getId(),
                storeProduct.getWarehouse().getCode(),
                storeProduct.getWarehouse().getName(),
                storeProduct.getProduct().getId(),
                storeProduct.getProduct().getSku(),
                storeProduct.getProduct().getName(),
                storeProduct.getQuantity(),
                storeProduct.getSaleStatus(),
                storeProduct.getSalePrice(),
                storeProduct.getUpdatedAt()
        );
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
