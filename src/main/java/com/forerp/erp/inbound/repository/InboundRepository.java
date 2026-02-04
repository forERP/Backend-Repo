package com.forerp.erp.inbound.repository;

import com.forerp.erp.inbound.domain.Inbound;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboundRepository extends JpaRepository<Inbound, Long> {

}