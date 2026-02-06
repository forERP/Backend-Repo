package com.forerp.erp.storeproduct.service;


import com.forerp.erp.store.dto.StoreProductListResponseDto;
import com.forerp.erp.storeproduct.repository.StoreProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreProductService {

    private final StoreProductRepository storeProductRepository;

    public Page<StoreProductListResponseDto> getStoreProductList(Long storeId, Pageable pageable){
        return storeProductRepository.findAllProductsWithStoreInfo(storeId, pageable);
    }
}
