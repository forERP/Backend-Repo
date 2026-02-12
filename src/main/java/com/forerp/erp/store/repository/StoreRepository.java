package com.forerp.erp.store.repository;

import com.forerp.erp.store.domain.Store;
import com.forerp.erp.store.domain.StoreType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository  extends JpaRepository<Store, Long> {
    Optional<Store> findFirstByStoreType(StoreType storeType);
    Optional<Store> findTopByStoreCodeNotOrderByStoreCodeDesc(String storeCodeNot);
}
