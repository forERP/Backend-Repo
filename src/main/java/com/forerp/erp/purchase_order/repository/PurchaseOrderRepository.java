package com.forerp.erp.purchase_order.repository;

import com.forerp.erp.purchase_order.domain.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

}
