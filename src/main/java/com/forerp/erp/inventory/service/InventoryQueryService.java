package com.forerp.erp.inventory.service;

import com.forerp.erp.inventory.dto.InventoryListResponse;
import com.forerp.erp.inventory.dto.InventoryResponse;
import com.forerp.erp.storeproduct.domain.StoreProduct;
import com.forerp.erp.storeproduct.repository.StoreProductRepository;
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

    public InventoryListResponse list(Long storeId, Long warehouseId, String keyword, int page, int size) {
        String kw = (keyword == null) ? "" : keyword.trim();
        PageRequest pageable = PageRequest.of(page, size);

        Page<StoreProduct> result = (warehouseId == null)
                ? storeProductRepository.findByStore_IdAndProduct_NameContaining(storeId, kw, pageable)
                : storeProductRepository.findByStore_IdAndWarehouse_IdAndProduct_NameContaining(storeId, warehouseId, kw, pageable);

        List<InventoryListResponse.InventoryItem> content = result.getContent().stream()
                .map(sp -> new InventoryListResponse.InventoryItem(
                        sp.getProduct().getId(),
                        sp.getProduct().getName(),
                        sp.getQuantity(),
                        sp.getUpdatedAt()
                ))
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
        StoreProduct sp = (warehouseId == null)
                ? storeProductRepository.findByStore_IdAndProduct_Id(storeId, productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 매장 재고를 찾을 수 없습니다."))
                : storeProductRepository.findByStore_IdAndWarehouse_IdAndProduct_Id(storeId, warehouseId, productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 창고 재고를 찾을 수 없습니다."));

        return new InventoryResponse(
                sp.getProduct().getId(),
                sp.getProduct().getName(),
                sp.getQuantity(),
                sp.getUpdatedAt()
        );
    }
}