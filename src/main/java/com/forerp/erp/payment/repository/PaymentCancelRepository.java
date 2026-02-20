package com.forerp.erp.payment.repository;

import com.forerp.erp.payment.domain.PaymentCancel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentCancelRepository extends JpaRepository<PaymentCancel, Long> {
}

