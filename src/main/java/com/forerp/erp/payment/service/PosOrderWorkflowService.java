package com.forerp.erp.payment.service;

import com.forerp.erp.order.domain.Order;
import com.forerp.erp.order.domain.OrderStatus;
import com.forerp.erp.order.dto.OrderResponse;
import com.forerp.erp.outbound.domain.Outbound;
import com.forerp.erp.outbound.domain.OutboundStatus;
import com.forerp.erp.outbound.dto.OutboundCreateRequest;
import com.forerp.erp.outbound.dto.OutboundResponse;
import com.forerp.erp.outbound.repository.OutboundRepository;
import com.forerp.erp.outbound.service.OutboundService;
import com.forerp.erp.payment.domain.Payment;
import com.forerp.erp.payment.domain.ServiceMode;
import com.forerp.erp.payment.dto.PaymentSummaryResponse;
import com.forerp.erp.payment.dto.PosOrderDetailResponse;
import com.forerp.erp.payment.dto.PosOrderListResponse;
import com.forerp.erp.payment.repository.PaymentRepository;
import com.forerp.erp.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class PosOrderWorkflowService {

    private final PaymentRepository paymentRepository;
    private final OutboundRepository outboundRepository;
    private final OutboundService outboundService;

    public PosOrderListResponse list(User actor, String orderStatus, int page, int size) {
        Long storeId = requireStoreId(actor);
        OrderStatus parsedStatus = parseOrderStatus(orderStatus);

        Page<Payment> result = paymentRepository.searchPosOrders(storeId, parsedStatus, PageRequest.of(page, size));

        List<Long> orderIds = result.getContent().stream()
                .map(payment -> payment.getOrder().getId())
                .toList();

        Map<Long, Outbound> outboundByOrderId = new HashMap<>();
        if (!orderIds.isEmpty()) {
            outboundRepository.findByOrder_IdIn(orderIds)
                    .forEach(outbound -> outboundByOrderId.put(outbound.getOrder().getId(), outbound));
        }

        List<PosOrderListResponse.PosOrderItem> content = result.getContent().stream()
                .map(payment -> {
                    Order order = payment.getOrder();
                    Outbound outbound = outboundByOrderId.get(order.getId());

                    return new PosOrderListResponse.PosOrderItem(
                            order.getId(),
                            payment.getId(),
                            payment.getMerchantOrderId(),
                            order.getStatus().name(),
                            payment.getStatus().name(),
                            payment.getServiceMode(),
                            order.getTotalAmount(),
                            order.getOrderedAt(),
                            outbound == null ? null : outbound.getStatus().name(),
                            outbound == null || outbound.getShipment() == null
                                    ? null
                                    : outbound.getShipment().getStatus().name()
                    );
                })
                .toList();

        return new PosOrderListResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public PosOrderDetailResponse getDetail(User actor, Long orderId) {
        Payment payment = loadPaymentByOrder(actor, orderId);
        Outbound outbound = outboundRepository.findByOrder_Id(orderId).orElse(null);

        return new PosOrderDetailResponse(
                OrderResponse.from(payment.getOrder()),
                PaymentSummaryResponse.from(payment),
                outbound == null ? null : OutboundResponse.from(outbound)
        );
    }

    public PosOrderDetailResponse prepare(User actor, Long orderId) {
        Payment payment = loadPaymentByOrder(actor, orderId);
        Order order = payment.getOrder();

        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("취소된 주문은 준비 처리할 수 없습니다.");
        }
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.ARRIVED) {
            return getDetail(actor, orderId);
        }

        Outbound outbound = outboundRepository.findByOrder_Id(orderId).orElse(null);
        if (outbound == null) {
            OutboundCreateRequest createRequest = buildOutboundCreateRequest(order);
            outbound = outboundService.createOutbound(createRequest);
        }

        return new PosOrderDetailResponse(
                OrderResponse.from(order),
                PaymentSummaryResponse.from(payment),
                OutboundResponse.from(outbound)
        );
    }

    public PosOrderDetailResponse confirm(User actor, Long orderId) {
        Payment payment = loadPaymentByOrder(actor, orderId);
        Order order = payment.getOrder();

        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("취소된 주문은 출고 확정할 수 없습니다.");
        }
        if (order.getStatus() == OrderStatus.ARRIVED) {
            return getDetail(actor, orderId);
        }

        Outbound outbound = outboundRepository.findByOrder_Id(orderId).orElse(null);
        if (outbound == null) {
            outbound = outboundService.createOutbound(buildOutboundCreateRequest(order));
        }

        if (outbound.getStatus() == OutboundStatus.CREATED) {
            outbound = outboundService.confirmOutbound(
                    outbound.getId(),
                    actor,
                    "POS",
                    buildPosTrackingNumber(outbound.getId())
            );
        }

        if (payment.getServiceMode() != ServiceMode.DELIVERY && outbound.getStatus() == OutboundStatus.CONFIRMED) {
            outbound = outboundService.arriveOutbound(outbound.getId());
        }

        return new PosOrderDetailResponse(
                OrderResponse.from(order),
                PaymentSummaryResponse.from(payment),
                OutboundResponse.from(outbound)
        );
    }

    private Payment loadPaymentByOrder(User actor, Long orderId) {
        Payment payment = paymentRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문에 연결된 결제가 없습니다."));

        Long actorStoreId = requireStoreId(actor);
        if (!actorStoreId.equals(payment.getStore().getId())) {
            throw new IllegalStateException("다른 매장의 주문에는 접근할 수 없습니다.");
        }
        return payment;
    }

    private Long requireStoreId(User actor) {
        if (actor == null || actor.getStore() == null || actor.getStore().getId() == null) {
            throw new IllegalStateException("매장 정보가 없는 사용자입니다.");
        }
        return actor.getStore().getId();
    }

    private OrderStatus parseOrderStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("유효하지 않은 주문 상태입니다. status=" + status);
        }
    }

    private OutboundCreateRequest buildOutboundCreateRequest(Order order) {
        List<OutboundCreateRequest.OutboundCreateItem> items = order.getItems().stream()
                .map(item -> new OutboundCreateRequest.OutboundCreateItem(item.getId(), item.getQuantity()))
                .toList();

        return new OutboundCreateRequest(
                order.getId(),
                order.getStore().getId(),
                order.getWarehouse().getId(),
                items
        );
    }

    private String buildPosTrackingNumber(Long outboundId) {
        return "POS-" + outboundId + "-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}

