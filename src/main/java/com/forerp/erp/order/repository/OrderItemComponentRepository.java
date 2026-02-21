package com.forerp.erp.order.repository;

import com.forerp.erp.order.domain.OrderItemComponent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OrderItemComponentRepository extends JpaRepository<OrderItemComponent, Long> {

    @EntityGraph(attributePaths = {"componentProduct", "componentProduct.category"})
    List<OrderItemComponent> findByOrderItem_IdIn(Collection<Long> orderItemIds);
}
