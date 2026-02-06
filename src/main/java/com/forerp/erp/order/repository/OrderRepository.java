package com.forerp.erp.order.repository;

import com.forerp.erp.order.domain.Order;
import com.forerp.erp.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"store", "items", "items.product"})
    @Query("""
        select o from Order o
        where (:storeId is null or o.store.id = :storeId)
          and (:status is null or o.status = :status)
          and (:fromDt is null or o.orderedAt >= :fromDt)
          and (:toDt is null or o.orderedAt < :toDt)
        """)
    Page<Order> search(
            @Param("storeId") Long storeId,
            @Param("status") OrderStatus status,
            @Param("fromDt") LocalDateTime fromDt,
            @Param("toDt") LocalDateTime toDt,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"store", "items", "items.product"})
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findDetailById(@Param("id") Long id);
}