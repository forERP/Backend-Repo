package com.forerp.erp.returns.service;

import com.forerp.erp.order.domain.Order;
import com.forerp.erp.order.domain.OrderStatus;
import com.forerp.erp.payment.domain.Payment;
import com.forerp.erp.payment.dto.PaymentCancelRequest;
import com.forerp.erp.payment.repository.PaymentRepository;
import com.forerp.erp.payment.service.PaymentService;
import com.forerp.erp.returns.domain.ReturnStatus;
import com.forerp.erp.returns.domain.SalesReturn;
import com.forerp.erp.returns.dto.ReturnListResponse;
import com.forerp.erp.returns.dto.ReturnProcessRequest;
import com.forerp.erp.returns.dto.ReturnResponse;
import com.forerp.erp.returns.repository.SalesReturnRepository;
import com.forerp.erp.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReturnService {

    private final SalesReturnRepository salesReturnRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public ReturnResponse process(User actor, ReturnProcessRequest request) {
        Payment payment = paymentRepository.findByOrder_Id(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("주문에 연결된 결제를 찾을 수 없습니다."));

        verifyStoreAccess(actor, payment.getStore().getId());
        Order order = requireOrder(payment);

        if (salesReturnRepository.existsByOrder_Id(order.getId())) {
            throw new IllegalStateException("이미 반품 처리된 주문입니다.");
        }

        if (!(order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.ARRIVED)) {
            throw new IllegalStateException("반품은 출고 이후 상태에서만 처리할 수 있습니다.");
        }

        BigDecimal canceledBefore = payment.getCanceledAmount();
        String orderStatusBefore = order.getStatus().name();

        PaymentCancelRequest cancelRequest = new PaymentCancelRequest();
        cancelRequest.setReason(request.getReason().trim());
        cancelRequest.setDiscardStock(Boolean.TRUE.equals(request.getDiscardStock()));

        paymentService.cancelForReturn(actor, payment.getId(), cancelRequest);

        BigDecimal refundedAmount = payment.getCanceledAmount().subtract(canceledBefore);
        if (refundedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("반품 환불 금액이 0원 이하입니다.");
        }

        SalesReturn salesReturn = SalesReturn.processed(
                order,
                payment,
                payment.getStore(),
                payment.getWarehouse(),
                actor,
                cancelRequest.getReason(),
                cancelRequest.getDiscardStock(),
                refundedAmount,
                orderStatusBefore,
                order.getStatus().name()
        );

        return ReturnResponse.from(salesReturnRepository.save(salesReturn));
    }

    @Transactional(readOnly = true)
    public ReturnListResponse list(User actor, Long storeId, String status, String from, String to, int page, int size) {
        Long resolvedStoreId = resolveRequestedStoreId(actor, storeId);
        ReturnStatus parsedStatus = parseStatus(status);
        LocalDateTime fromDt = parseFrom(from);
        LocalDateTime toDt = parseToExclusive(to);

        Page<SalesReturn> result = salesReturnRepository.search(
                resolvedStoreId,
                parsedStatus,
                fromDt,
                toDt,
                PageRequest.of(page, size)
        );

        List<ReturnListResponse.ReturnListItem> content = result.getContent().stream()
                .map(item -> new ReturnListResponse.ReturnListItem(
                        item.getId(),
                        item.getOrder().getId(),
                        item.getPayment().getId(),
                        item.getStore().getId(),
                        item.getStore().getName(),
                        item.getStore().getStoreCode(),
                        item.getStatus().name(),
                        item.isDiscardStock(),
                        item.getRefundedAmount(),
                        item.getOrderStatusBefore(),
                        item.getOrderStatusAfter(),
                        item.getProcessedBy() == null ? null : item.getProcessedBy().getName(),
                        item.getProcessedAt(),
                        item.getReason()
                ))
                .toList();

        return new ReturnListResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public ReturnResponse get(User actor, Long returnId) {
        SalesReturn salesReturn = salesReturnRepository.findById(returnId)
                .orElseThrow(() -> new IllegalArgumentException("반품 내역을 찾을 수 없습니다."));
        verifyStoreAccess(actor, salesReturn.getStore().getId());
        return ReturnResponse.from(salesReturn);
    }

    @Transactional(readOnly = true)
    public Optional<ReturnResponse> getByOrderId(User actor, Long orderId) {
        return salesReturnRepository.findByOrder_Id(orderId)
                .map(salesReturn -> {
                    verifyStoreAccess(actor, salesReturn.getStore().getId());
                    return ReturnResponse.from(salesReturn);
                });
    }

    private Order requireOrder(Payment payment) {
        if (payment.getOrder() == null) {
            throw new IllegalStateException("결제에 연결된 주문이 없습니다.");
        }
        return payment.getOrder();
    }

    private void verifyStoreAccess(User actor, Long targetStoreId) {
        if (actor == null || actor.getStore() == null || actor.getStore().getId() == null) {
            return;
        }
        if (!actor.getStore().getId().equals(targetStoreId)) {
            throw new IllegalStateException("다른 매장의 반품 내역에는 접근할 수 없습니다.");
        }
    }

    private Long resolveRequestedStoreId(User actor, Long requestedStoreId) {
        if (actor == null || actor.getStore() == null || actor.getStore().getId() == null) {
            return requestedStoreId;
        }

        Long actorStoreId = actor.getStore().getId();
        if (requestedStoreId == null) {
            return actorStoreId;
        }
        if (!actorStoreId.equals(requestedStoreId)) {
            throw new IllegalStateException("다른 매장의 반품 내역에는 접근할 수 없습니다.");
        }
        return actorStoreId;
    }

    private ReturnStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return ReturnStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("유효하지 않은 반품 상태입니다. status=" + status);
        }
    }

    private LocalDateTime parseFrom(String from) {
        if (from == null || from.isBlank()) {
            return null;
        }
        return java.time.LocalDate.parse(from).atStartOfDay();
    }

    private LocalDateTime parseToExclusive(String to) {
        if (to == null || to.isBlank()) {
            return null;
        }
        return java.time.LocalDate.parse(to).plusDays(1).atStartOfDay();
    }
}
