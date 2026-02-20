package com.forerp.erp.payment.repository;

import com.forerp.erp.order.domain.OrderStatus;
import com.forerp.erp.payment.domain.Payment;
import com.forerp.erp.payment.domain.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = {"store", "warehouse", "order", "order.items", "order.items.product"})
    Optional<Payment> findByMerchantOrderId(String merchantOrderId);

    @EntityGraph(attributePaths = {"store", "warehouse", "order", "order.items", "order.items.product"})
    Optional<Payment> findByOrder_Id(Long orderId);

    @EntityGraph(attributePaths = {"store", "warehouse", "order"})
    Optional<Payment> findByPaymentKey(String paymentKey);

    boolean existsByMerchantOrderId(String merchantOrderId);

    @EntityGraph(attributePaths = {"store", "warehouse", "order"})
    @Query("""
        select p from Payment p
        where (:storeId is null or p.store.id = :storeId)
          and (:status is null or p.status = :status)
          and (:fromDt is null or p.preparedAt >= :fromDt)
          and (:toDt is null or p.preparedAt < :toDt)
        order by p.preparedAt desc
        """)
    Page<Payment> search(
            @Param("storeId") Long storeId,
            @Param("status") PaymentStatus status,
            @Param("fromDt") LocalDateTime fromDt,
            @Param("toDt") LocalDateTime toDt,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"store", "warehouse", "order"})
    @Query("""
        select p from Payment p
        join p.order o
        where p.store.id = :storeId
          and (:orderStatus is null or o.status = :orderStatus)
          and p.status in (
                com.forerp.erp.payment.domain.PaymentStatus.DONE,
                com.forerp.erp.payment.domain.PaymentStatus.PARTIAL_CANCELED,
                com.forerp.erp.payment.domain.PaymentStatus.CANCELED
          )
        order by p.preparedAt desc
        """)
    Page<Payment> searchPosOrders(
            @Param("storeId") Long storeId,
            @Param("orderStatus") OrderStatus orderStatus,
            Pageable pageable
    );
}
