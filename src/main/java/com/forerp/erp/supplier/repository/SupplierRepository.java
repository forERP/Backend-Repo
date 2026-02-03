package com.forerp.erp.supplier.repository;

import com.forerp.erp.supplier.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

}