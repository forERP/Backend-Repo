package com.forerp.erp.warehouse.repository;

import com.forerp.erp.warehouse.domain.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    List<Warehouse> findByStore_Id(Long storeId);

    @EntityGraph(attributePaths = {"store"})
    @Query("""
        select w from Warehouse w
        where (:name is null or lower(w.name) like lower(concat('%', :name, '%')))
          and (:code is null or lower(w.code) like lower(concat('%', :code, '%')))
          and (:active is null or w.active = :active)
        """)
    Page<Warehouse> search(
            @Param("name") String name,
            @Param("code") String code,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
