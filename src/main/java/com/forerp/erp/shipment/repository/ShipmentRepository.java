package com.forerp.erp.shipment.repository;

import com.forerp.erp.shipment.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

}