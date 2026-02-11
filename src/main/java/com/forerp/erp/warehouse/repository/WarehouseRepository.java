package com.forerp.erp.warehouse.repository;

import com.forerp.erp.warehouse.domain.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    List<Warehouse> findByStore_Id(Long storeId);
}