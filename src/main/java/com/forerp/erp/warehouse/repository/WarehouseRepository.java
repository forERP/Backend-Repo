package com.forerp.erp.warehouse.repository;

import com.forerp.erp.warehouse.domain.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    List<Warehouse> findByStore_Id(Long storeId);
    List<Warehouse> findByStore_IdAndActiveTrue(Long storeId);
    List<Warehouse> findByActiveTrue();
    Optional<Warehouse> findByStore_IdAndCode(Long storeId, String code);

    @EntityGraph(attributePaths = {"store"})
    @Query("""
        select w from Warehouse w
        where (
              :keyword is null
              or lower(w.name) like lower(concat('%', :keyword, '%'))
              or lower(w.code) like lower(concat('%', :keyword, '%'))
          )
          and (:name is null or lower(w.name) like lower(concat('%', :name, '%')))
          and (:code is null or lower(w.code) like lower(concat('%', :code, '%')))
          and (:active is null or w.active = :active)
        """)
    Page<Warehouse> search(
            @Param("keyword") String keyword,
            @Param("name") String name,
            @Param("code") String code,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
