package com.forerp.erp.store.service.support;

import com.forerp.erp.store.domain.Store;
import com.forerp.erp.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StoreReader {

    private final StoreRepository storeRepository;

    public Store getStore(Long id){
        return storeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));
    }

    public List<Store> getAllStores(){
        return storeRepository.findAll();
    }

    // 매장 코드 카운트 (순차적 코드 발급)
    public long countAll(){
        return storeRepository.count();
    }
}
