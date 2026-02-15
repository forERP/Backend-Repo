package com.forerp.erp.supplier.repository;

import com.forerp.erp.supplier.domain.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    @Query("""
        select s from Supplier s
        where (:name is null or lower(s.name) like lower(concat('%', :name, '%')))
          and (:contactName is null or lower(s.contactName) like lower(concat('%', :contactName, '%')))
          and (:active is null or s.active = :active)
        """)
    Page<Supplier> search(
            @Param("name") String name,
            @Param("contactName") String contactName,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
