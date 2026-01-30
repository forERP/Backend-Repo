package com.forerp.erp.inventory.repository;

import com.forerp.erp.inventory.domain.InventoryHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryHistoryRepository extends JpaRepository<InventoryHistory, Long> {

}
