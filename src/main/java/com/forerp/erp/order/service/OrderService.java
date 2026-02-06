package com.forerp.erp.order.service;

import com.forerp.erp.common.query.QueryParamParser;
import com.forerp.erp.order.domain.Order;
import com.forerp.erp.order.domain.OrderStatus;
import com.forerp.erp.order.dto.OrderCreateRequest;
import com.forerp.erp.order.dto.OrderListResponse;
import com.forerp.erp.order.repository.OrderRepository;
import com.forerp.erp.order.service.support.OrderBuilder;
import com.forerp.erp.order.service.support.OrderLoader;
import com.forerp.erp.outbound.repository.OutboundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboundRepository outboundRepository;
    private final OrderLoader loader;
    private final OrderBuilder builder;

    /* 주문 생성 */
    public Order create(OrderCreateRequest req) {
        Order order = builder.buildOrderAggregate(req);

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order get(Long orderId) {
        return loader.loadOrderDetail(orderId);
    }

    /* 주문 취소: 출고가 생성되기 전까지 */
    public Order cancel(Long orderId) {
        Order order = loader.loadOrderDetail(orderId);

        if (outboundRepository.existsByOrder_Id(orderId)) {
            throw new IllegalStateException("출고 문서가 생성된 주문은 취소할 수 없습니다.");
        }

        order.cancel();
        return order;
    }

    @Transactional(readOnly = true)
    public OrderListResponse list(
            Long storeId,
            String status,
            String from,
            String to,
            int page,
            int size
    ) {
        OrderStatus st = QueryParamParser.parseEnumOrNull(status, OrderStatus.class, "status");
        LocalDateTime fromDt = QueryParamParser.parseFromDate(from);
        LocalDateTime toDt = QueryParamParser.parseToDateExclusive(to);

        PageRequest pageable = PageRequest.of(page, size);
        Page<Order> result = orderRepository.search(storeId, st, fromDt, toDt, pageable);

        List<OrderListResponse.OrderListItem> content = result.getContent().stream()
                .map(o -> new OrderListResponse.OrderListItem(
                        o.getId(),
                        o.getStore().getId(),
                        o.getStatus().name(),
                        o.getTotalAmount(),
                        o.getOrderedAt()
                ))
                .toList();

        return new OrderListResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}