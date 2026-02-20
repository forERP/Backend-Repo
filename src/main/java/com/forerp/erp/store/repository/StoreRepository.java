package com.forerp.erp.store.repository;

import com.forerp.erp.store.domain.Store;
import com.forerp.erp.store.domain.StoreStatus;
import com.forerp.erp.store.domain.StoreType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StoreRepository  extends JpaRepository<Store, Long> {
    Optional<Store> findFirstByStoreType(StoreType storeType);
    Optional<Store> findTopByStoreCodeNotOrderByStoreCodeDesc(String storeCodeNot);

    @Query("""
        select s from Store s
        where (
              :keyword is null
              or lower(s.name) like lower(concat('%', :keyword, '%'))
              or lower(s.storeCode) like lower(concat('%', :keyword, '%'))
          )
          and (:name is null or lower(s.name) like lower(concat('%', :name, '%')))
          and (:code is null or lower(s.storeCode) like lower(concat('%', :code, '%')))
          and (:status is null or s.status = :status)
        """)
    Page<Store> search(
            @Param("keyword") String keyword,
            @Param("name") String name,
            @Param("code") String code,
            @Param("status") StoreStatus status,
            Pageable pageable
    );
}
