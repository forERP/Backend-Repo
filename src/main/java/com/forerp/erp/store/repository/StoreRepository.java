package com.forerp.erp.store.repository;

import com.forerp.erp.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository  extends JpaRepository<Store, Long> {

}
