package com.forerp.erp.payment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forerp.erp.inventory.domain.InventoryHistory;
import com.forerp.erp.inventory.domain.RefType;
import com.forerp.erp.inventory.repository.InventoryHistoryRepository;
import com.forerp.erp.order.domain.Order;
import com.forerp.erp.order.domain.OrderItem;
import com.forerp.erp.order.domain.OrderStatus;
import com.forerp.erp.order.dto.OrderResponse;
import com.forerp.erp.order.repository.OrderRepository;
import com.forerp.erp.outbound.domain.Outbound;
import com.forerp.erp.outbound.domain.OutboundItem;
import com.forerp.erp.outbound.domain.OutboundStatus;
import com.forerp.erp.outbound.repository.OutboundRepository;
import com.forerp.erp.payment.domain.Payment;
import com.forerp.erp.payment.domain.PaymentCancel;
import com.forerp.erp.payment.domain.PaymentStatus;
import com.forerp.erp.payment.domain.ServiceMode;
import com.forerp.erp.payment.dto.PaymentCancelRequest;
import com.forerp.erp.payment.dto.PaymentCancelResponse;
import com.forerp.erp.payment.dto.PaymentConfirmRequest;
import com.forerp.erp.payment.dto.PaymentConfirmResponse;
import com.forerp.erp.payment.dto.PaymentListResponse;
import com.forerp.erp.payment.dto.PaymentPrepareRequest;
import com.forerp.erp.payment.dto.PaymentPrepareResponse;
import com.forerp.erp.payment.dto.PaymentSummaryResponse;
import com.forerp.erp.payment.repository.PaymentCancelRepository;
import com.forerp.erp.payment.repository.PaymentRepository;
import com.forerp.erp.product.domain.Product;
import com.forerp.erp.product.repository.ProductRepository;
import com.forerp.erp.realtime.service.RealtimeEventService;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.storeproduct.domain.StoreProduct;
import com.forerp.erp.storeproduct.repository.StoreProductRepository;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.warehouse.domain.Warehouse;
import com.forerp.erp.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private static final TypeReference<List<OrderSnapshotLine>> SNAPSHOT_LINE_LIST_TYPE = new TypeReference<>() {
    };

    private final PaymentRepository paymentRepository;
    private final PaymentCancelRepository paymentCancelRepository;
    private final OrderRepository orderRepository;
    private final OutboundRepository outboundRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StoreProductRepository storeProductRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;
    private final OrderStockAllocator orderStockAllocator;
    private final TossPaymentsClient tossPaymentsClient;
    private final ObjectMapper objectMapper;
    private final RealtimeEventService realtimeEventService;

    @Value("${toss.api.client-key:}")
    private String tossClientKey;

    public PaymentPrepareResponse prepare(User actor, PaymentPrepareRequest request) {
        Store store = requireStore(actor);

        List<OrderStockAllocator.RequestedLine> requestedLines = request.getItems().stream()
                .map(item -> new OrderStockAllocator.RequestedLine(item.getProductId(), item.getQty()))
                .toList();

        OrderStockAllocator.AllocationResult allocation = orderStockAllocator.allocate(store, requestedLines);

        String merchantOrderId = generateMerchantOrderId();
        String customerKey = "cust_" + UUID.randomUUID().toString().replace("-", "");
        String orderName = buildOrderName(allocation.lines());
        String snapshot = writeOrderSnapshot(allocation.lines());

        Payment payment = Payment.prepare(
                store,
                merchantOrderId,
                customerKey,
                orderName,
                allocation.totalAmount(),
                request.getServiceMode(),
                snapshot
        );
        payment.assignWarehouse(allocation.warehouse());

        Payment saved = paymentRepository.save(payment);
        realtimeEventService.publishPaymentChanged(saved.getStore().getId(), saved.getId(), "prepared");
        return new PaymentPrepareResponse(
                saved.getId(),
                saved.getMerchantOrderId(),
                saved.getCustomerKey(),
                saved.getOrderName(),
                saved.getAmount(),
                saved.getCurrency(),
                saved.getServiceMode(),
                tossClientKey
        );
    }

    public PaymentConfirmResponse confirm(User actor, PaymentConfirmRequest request) {
        Payment payment = paymentRepository.findByMerchantOrderId(request.getMerchantOrderId())
                .orElseThrow(() -> new IllegalArgumentException("결제 준비 정보를 찾을 수 없습니다."));
        verifyPaymentAccess(payment, actor);

        if (payment.getStatus() == PaymentStatus.DONE) {
            boolean orderWasMissing = payment.getOrder() == null;
            Order order = ensureOrderCreated(payment, actor);
            Payment savedPayment = paymentRepository.save(payment);
            if (orderWasMissing) {
                realtimeEventService.publishPaymentChanged(savedPayment.getStore().getId(), savedPayment.getId(), "confirmed_from_webhook");
                realtimeEventService.publishOrderChanged(savedPayment.getStore().getId(), order.getId(), "placed");
            }
            return new PaymentConfirmResponse(
                    PaymentSummaryResponse.from(savedPayment),
                    OrderResponse.from(order)
            );
        }

        if (payment.getStatus() != PaymentStatus.READY && payment.getStatus() != PaymentStatus.FAILED) {
            throw new IllegalStateException("승인 가능한 결제 상태가 아닙니다. status=" + payment.getStatus());
        }

        if (payment.getAmount().compareTo(request.getAmount()) != 0) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        JsonNode confirmResult = tossPaymentsClient.confirm(
                request.getPaymentKey(),
                request.getMerchantOrderId(),
                request.getAmount()
        );

        Order order;
        try {
            order = ensureOrderCreated(payment, actor);
        } catch (Exception ex) {
            JsonNode cancelResult = tossPaymentsClient.cancel(
                    request.getPaymentKey(),
                    "재고 변동으로 주문 생성에 실패하여 자동 취소되었습니다.",
                    request.getAmount()
            );
            payment.markCanceled(request.getAmount(), text(cancelResult, "status"), cancelResult.toString());
            throw new IllegalStateException("결제는 승인되었지만 재고 사정으로 주문 생성에 실패하여 자동 취소되었습니다.", ex);
        }

        payment.markDone(
                text(confirmResult, "paymentKey"),
                text(confirmResult, "method"),
                text(confirmResult, "status"),
                decimal(confirmResult, "totalAmount"),
                confirmResult.toString()
        );
        payment.assignOrder(order);

        Payment savedPayment = paymentRepository.save(payment);
        realtimeEventService.publishPaymentChanged(savedPayment.getStore().getId(), savedPayment.getId(), "confirmed");
        if (savedPayment.getOrder() != null) {
            realtimeEventService.publishOrderChanged(savedPayment.getStore().getId(), savedPayment.getOrder().getId(), "placed");
        }

        return new PaymentConfirmResponse(
                PaymentSummaryResponse.from(savedPayment),
                OrderResponse.from(order)
        );
    }

    public PaymentCancelResponse cancel(User actor, Long paymentId, PaymentCancelRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제를 찾을 수 없습니다."));
        verifyPaymentAccess(payment, actor);

        if (payment.getPaymentKey() == null || payment.getPaymentKey().isBlank()) {
            throw new IllegalStateException("아직 승인되지 않은 결제입니다.");
        }

        if (payment.getOrder() == null) {
            throw new IllegalStateException("연결된 주문 정보가 없습니다.");
        }

        Order order = payment.getOrder();
        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }

        Map<Long, Integer> cancelMap = normalizeCancelItems(request, order);
        boolean fullCancel = isFullCancel(cancelMap, order);
        validateCancelPolicy(order.getStatus(), fullCancel);

        BigDecimal cancelAmount = computeCancelAmount(order, cancelMap);
        if (cancelAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("취소 금액이 0 이하입니다.");
        }
        if (payment.getRemainingCancelableAmount().compareTo(cancelAmount) < 0) {
            throw new IllegalStateException("취소 가능 금액을 초과했습니다.");
        }

        Outbound outbound = outboundRepository.findByOrder_Id(order.getId()).orElse(null);
        boolean discardStock = Boolean.TRUE.equals(request.getDiscardStock());
        preValidateStockAdjustment(order, outbound, discardStock);

        JsonNode cancelResult = tossPaymentsClient.cancel(
                payment.getPaymentKey(),
                request.getReason(),
                cancelAmount
        );

        String orderStatusBefore = order.getStatus().name();
        boolean stockRefunded = false;

        if (order.getStatus() == OrderStatus.PLACED) {
            List<Order.OrderItemCancelCommand> commands = cancelMap.entrySet().stream()
                    .map(entry -> new Order.OrderItemCancelCommand(entry.getKey(), entry.getValue()))
                    .toList();
            order.cancelPlacedItems(commands);
        } else if (order.getStatus() == OrderStatus.PREPARED) {
            if (discardStock) {
                applyDiscardStock(order, outbound, actor);
            }
            if (outbound != null && outbound.getStatus() == OutboundStatus.CREATED) {
                outbound.cancel();
            }
            order.markCanceledByRefund();
        } else if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.ARRIVED) {
            if (!discardStock) {
                applyReturnStock(order, outbound, actor);
                stockRefunded = true;
            }
            order.markCanceledByRefund();
        } else {
            throw new IllegalStateException("지원하지 않는 주문 상태입니다. status=" + order.getStatus());
        }

        payment.markCanceled(cancelAmount, text(cancelResult, "status"), cancelResult.toString());

        PaymentCancel paymentCancel = PaymentCancel.create(
                payment,
                extractCancelKey(cancelResult),
                request.getReason(),
                cancelAmount,
                discardStock,
                stockRefunded,
                orderStatusBefore,
                cancelResult.toString()
        );
        paymentCancelRepository.save(paymentCancel);
        realtimeEventService.publishPaymentChanged(payment.getStore().getId(), payment.getId(), "canceled");
        realtimeEventService.publishOrderChanged(payment.getStore().getId(), order.getId(), "canceled");
        if (("PREPARED".equals(orderStatusBefore) && discardStock)
                || (("SHIPPED".equals(orderStatusBefore) || "ARRIVED".equals(orderStatusBefore)) && stockRefunded)) {
            realtimeEventService.publishInventoryChanged(payment.getStore().getId(), "payment_cancel_stock_adjusted");
        }

        return new PaymentCancelResponse(
                PaymentSummaryResponse.from(payment),
                OrderResponse.from(order),
                discardStock
        );
    }

    @Transactional(readOnly = true)
    public PaymentSummaryResponse get(User actor, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제를 찾을 수 없습니다."));
        verifyPaymentAccess(payment, actor);
        return PaymentSummaryResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PaymentSummaryResponse getByOrderId(User actor, Long orderId) {
        Payment payment = paymentRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문에 연결된 결제를 찾을 수 없습니다."));
        verifyPaymentAccess(payment, actor);
        return PaymentSummaryResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PaymentListResponse list(
            User actor,
            Long storeId,
            String status,
            String from,
            String to,
            int page,
            int size
    ) {
        Long resolvedStoreId = resolveRequestedStoreId(actor, storeId);
        PaymentStatus paymentStatus = parseStatus(status);
        LocalDateTime fromDt = parseFrom(from);
        LocalDateTime toDt = parseToExclusive(to);

        Page<Payment> result = paymentRepository.search(
                resolvedStoreId,
                paymentStatus,
                fromDt,
                toDt,
                PageRequest.of(page, size)
        );

        List<PaymentSummaryResponse> content = result.getContent().stream()
                .map(PaymentSummaryResponse::from)
                .toList();

        return new PaymentListResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public void handleWebhook(String body) {
        JsonNode payload;
        try {
            payload = objectMapper.readTree(body);
        } catch (Exception ex) {
            throw new IllegalArgumentException("웹훅 payload 파싱에 실패했습니다.");
        }

        String eventType = text(payload, "eventType");
        JsonNode data = payload.path("data");
        String orderId = text(data, "orderId");
        String paymentKey = text(data, "paymentKey");

        Payment payment = null;
        if (orderId != null && !orderId.isBlank()) {
            payment = paymentRepository.findByMerchantOrderId(orderId).orElse(null);
        }
        if (payment == null && paymentKey != null && !paymentKey.isBlank()) {
            payment = paymentRepository.findByPaymentKey(paymentKey).orElse(null);
        }
        if (payment == null) {
            return;
        }

        boolean changed = false;

        if ("PAYMENT_STATUS_CHANGED".equalsIgnoreCase(eventType)) {
            String tossStatus = text(data, "status");
            if ("DONE".equalsIgnoreCase(tossStatus) && payment.getStatus() == PaymentStatus.READY) {
                payment.markDone(
                        text(data, "paymentKey"),
                        text(data, "method"),
                        tossStatus,
                        decimal(data, "totalAmount"),
                        payload.toString()
                );
                changed = true;
            } else if ("CANCELED".equalsIgnoreCase(tossStatus)) {
                BigDecimal canceledAmount = decimal(data, "canceledAmount");
                if (canceledAmount.compareTo(BigDecimal.ZERO) > 0) {
                    payment.markCanceled(canceledAmount, tossStatus, payload.toString());
                    changed = true;
                }
            } else if ("ABORTED".equalsIgnoreCase(tossStatus) || "EXPIRED".equalsIgnoreCase(tossStatus)) {
                payment.markFailed(text(data, "code"), text(data, "message"), payload.toString());
                changed = true;
            }
        }

        if (changed) {
            realtimeEventService.publishPaymentChanged(payment.getStore().getId(), payment.getId(), "webhook_status_changed");
        }
    }

    private Order ensureOrderCreated(Payment payment, User actor) {
        if (payment.getOrder() != null) {
            return payment.getOrder();
        }

        List<OrderSnapshotLine> snapshotLines = readOrderSnapshot(payment.getOrderSnapshot());
        Warehouse warehouse = resolveWarehouseAtConfirm(payment.getStore(), payment.getWarehouse(), snapshotLines);

        Map<Long, Product> productMap = productRepository.findAllById(
                        snapshotLines.stream().map(OrderSnapshotLine::productId).toList()
                ).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        List<OrderItem> orderItems = snapshotLines.stream()
                .map(line -> {
                    Product product = productMap.get(line.productId());
                    if (product == null) {
                        throw new IllegalStateException("상품을 찾을 수 없습니다. productId=" + line.productId());
                    }
                    return OrderItem.create(product, line.qty(), line.unitPrice());
                })
                .toList();

        Order order = Order.create(payment.getStore(), warehouse, orderItems);
        orderRepository.save(order);

        payment.assignWarehouse(warehouse);
        payment.assignOrder(order);
        return order;
    }

    private Warehouse resolveWarehouseAtConfirm(Store store, Warehouse preferredWarehouse, List<OrderSnapshotLine> lines) {
        List<Warehouse> warehouses = warehouseRepository.findByStore_IdAndActiveTrue(store.getId()).stream()
                .sorted(Comparator
                        .comparing(Warehouse::getCode, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(Warehouse::getId))
                .toList();

        if (warehouses.isEmpty()) {
            throw new IllegalStateException("활성 창고가 없습니다.");
        }

        List<Warehouse> candidates = new ArrayList<>();
        if (preferredWarehouse != null) {
            candidates.add(preferredWarehouse);
        }
        for (Warehouse warehouse : warehouses) {
            if (preferredWarehouse == null || !warehouse.getId().equals(preferredWarehouse.getId())) {
                candidates.add(warehouse);
            }
        }

        List<Long> productIds = lines.stream().map(OrderSnapshotLine::productId).toList();

        for (Warehouse candidate : candidates) {
            List<StoreProduct> stockList = storeProductRepository.findByStore_IdAndWarehouse_IdAndProduct_IdIn(
                    store.getId(),
                    candidate.getId(),
                    productIds
            );
            Map<Long, StoreProduct> stockMap = stockList.stream()
                    .collect(Collectors.toMap(sp -> sp.getProduct().getId(), sp -> sp));

            boolean match = true;
            for (OrderSnapshotLine line : lines) {
                StoreProduct sp = stockMap.get(line.productId());
                if (sp == null || sp.getSaleStatus() != com.forerp.erp.storeproduct.domain.SaleStatus.ON) {
                    match = false;
                    break;
                }
                if (sp.getQuantity() < line.qty()) {
                    match = false;
                    break;
                }
                BigDecimal currentPrice = resolveUnitPrice(sp);
                if (currentPrice.compareTo(line.unitPrice()) != 0) {
                    match = false;
                    break;
                }
            }

            if (match) {
                return candidate;
            }
        }

        throw new IllegalStateException("결제 금액과 일치하는 재고를 확보할 수 없습니다.");
    }

    private BigDecimal resolveUnitPrice(StoreProduct sp) {
        if (sp.getSalePrice() != null && sp.getSalePrice().compareTo(BigDecimal.ZERO) > 0) {
            return sp.getSalePrice();
        }
        if (sp.getProduct().getMsrpPrice() != null) {
            return sp.getProduct().getMsrpPrice();
        }
        return BigDecimal.ZERO;
    }

    private void preValidateStockAdjustment(Order order, Outbound outbound, boolean discardStock) {
        if (order.getStatus() != OrderStatus.PREPARED || !discardStock) {
            return;
        }

        if (outbound != null) {
            for (OutboundItem item : outbound.getItems()) {
                if (item.getStoreProduct().getQuantity() < item.getQuantity()) {
                    throw new IllegalStateException("폐기 처리할 재고가 부족합니다.");
                }
            }
            return;
        }

        for (OrderItem orderItem : order.getItems()) {
            StoreProduct sp = storeProductRepository
                    .findByStore_IdAndWarehouse_IdAndProduct_Id(
                            order.getStore().getId(),
                            order.getWarehouse().getId(),
                            orderItem.getProduct().getId()
                    )
                    .orElseThrow(() -> new IllegalStateException("재고 정보를 찾을 수 없습니다."));

            if (sp.getQuantity() < orderItem.getQuantity()) {
                throw new IllegalStateException("폐기 처리할 재고가 부족합니다.");
            }
        }
    }

    private void applyDiscardStock(Order order, Outbound outbound, User actor) {
        if (outbound != null) {
            for (OutboundItem item : outbound.getItems()) {
                InventoryHistory history = item.getStoreProduct().decreaseStock(
                        item.getQuantity(),
                        RefType.DISCARD,
                        order.getId(),
                        item.getId(),
                        actor
                );
                inventoryHistoryRepository.save(history);
            }
            return;
        }

        for (OrderItem orderItem : order.getItems()) {
            StoreProduct sp = storeProductRepository
                    .findByStore_IdAndWarehouse_IdAndProduct_Id(
                            order.getStore().getId(),
                            order.getWarehouse().getId(),
                            orderItem.getProduct().getId()
                    )
                    .orElseThrow(() -> new IllegalStateException("재고 정보를 찾을 수 없습니다."));

            InventoryHistory history = sp.decreaseStock(
                    orderItem.getQuantity(),
                    RefType.DISCARD,
                    order.getId(),
                    orderItem.getId(),
                    actor
            );
            inventoryHistoryRepository.save(history);
        }
    }

    private void applyReturnStock(Order order, Outbound outbound, User actor) {
        if (outbound == null) {
            throw new IllegalStateException("출고 정보가 없어 재고 복원 처리를 할 수 없습니다.");
        }
        if (!(outbound.getStatus() == OutboundStatus.CONFIRMED || outbound.getStatus() == OutboundStatus.ARRIVED)) {
            throw new IllegalStateException("재고 복원은 출고 확정 이후 상태에서만 가능합니다.");
        }

        for (OutboundItem item : outbound.getItems()) {
            InventoryHistory history = item.getStoreProduct().increaseStock(
                    item.getQuantity(),
                    RefType.RETURN,
                    order.getId(),
                    item.getId(),
                    actor
            );
            inventoryHistoryRepository.save(history);
        }
    }

    private Map<Long, Integer> normalizeCancelItems(PaymentCancelRequest request, Order order) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            Map<Long, Integer> fullMap = new LinkedHashMap<>();
            for (OrderItem item : order.getItems()) {
                fullMap.put(item.getId(), item.getQuantity());
            }
            return fullMap;
        }

        Map<Long, Integer> normalized = new LinkedHashMap<>();
        Map<Long, Integer> orderedQtyByItemId = order.getItems().stream()
                .collect(Collectors.toMap(OrderItem::getId, OrderItem::getQuantity));

        for (PaymentCancelRequest.PaymentCancelItem item : request.getItems()) {
            Integer orderedQty = orderedQtyByItemId.get(item.getOrderItemId());
            if (orderedQty == null) {
                throw new IllegalArgumentException("주문 항목을 찾을 수 없습니다. orderItemId=" + item.getOrderItemId());
            }

            int mergedQty = normalized.getOrDefault(item.getOrderItemId(), 0) + item.getQty();
            if (mergedQty > orderedQty) {
                throw new IllegalArgumentException("취소 수량이 주문 수량을 초과했습니다. orderItemId=" + item.getOrderItemId());
            }
            normalized.put(item.getOrderItemId(), mergedQty);
        }

        return normalized;
    }

    private boolean isFullCancel(Map<Long, Integer> cancelMap, Order order) {
        if (cancelMap.size() != order.getItems().size()) {
            return false;
        }

        for (OrderItem item : order.getItems()) {
            Integer qty = cancelMap.get(item.getId());
            if (qty == null || qty != item.getQuantity()) {
                return false;
            }
        }
        return true;
    }

    private void validateCancelPolicy(OrderStatus orderStatus, boolean fullCancel) {
        if (orderStatus == OrderStatus.PLACED) {
            return;
        }

        if (!fullCancel) {
            throw new IllegalStateException("부분 취소는 PLACED 상태에서만 가능합니다.");
        }

        if (orderStatus == OrderStatus.PREPARED || orderStatus == OrderStatus.SHIPPED || orderStatus == OrderStatus.ARRIVED) {
            return;
        }

        throw new IllegalStateException("취소 가능한 주문 상태가 아닙니다. status=" + orderStatus);
    }

    private BigDecimal computeCancelAmount(Order order, Map<Long, Integer> cancelMap) {
        BigDecimal cancelAmount = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            Integer cancelQty = cancelMap.get(item.getId());
            if (cancelQty == null || cancelQty <= 0) {
                continue;
            }
            cancelAmount = cancelAmount.add(item.getUnitPrice().multiply(BigDecimal.valueOf(cancelQty)));
        }
        return cancelAmount;
    }

    private String writeOrderSnapshot(List<OrderStockAllocator.AllocatedLine> lines) {
        try {
            List<OrderSnapshotLine> snapshotLines = lines.stream()
                    .map(line -> new OrderSnapshotLine(line.productId(), line.qty(), line.unitPrice()))
                    .toList();
            return objectMapper.writeValueAsString(snapshotLines);
        } catch (Exception ex) {
            throw new IllegalStateException("주문 스냅샷 직렬화에 실패했습니다.", ex);
        }
    }

    private List<OrderSnapshotLine> readOrderSnapshot(String snapshot) {
        try {
            return objectMapper.readValue(snapshot, SNAPSHOT_LINE_LIST_TYPE);
        } catch (Exception ex) {
            throw new IllegalStateException("주문 스냅샷 파싱에 실패했습니다.", ex);
        }
    }

    private String buildOrderName(List<OrderStockAllocator.AllocatedLine> lines) {
        if (lines == null || lines.isEmpty()) {
            return "상품 주문";
        }

        String firstName = lines.get(0).productName();
        if (lines.size() == 1) {
            return firstName;
        }
        return firstName + " 외 " + (lines.size() - 1) + "건";
    }

    private String extractCancelKey(JsonNode cancelResult) {
        JsonNode cancels = cancelResult.path("cancels");
        if (cancels.isArray() && !cancels.isEmpty()) {
            JsonNode last = cancels.get(cancels.size() - 1);
            String transactionKey = text(last, "transactionKey");
            if (transactionKey != null && !transactionKey.isBlank()) {
                return transactionKey;
            }
        }
        return "cancel_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String generateMerchantOrderId() {
        String orderId = "POS-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        if (paymentRepository.existsByMerchantOrderId(orderId)) {
            return generateMerchantOrderId();
        }
        return orderId;
    }

    private Store requireStore(User actor) {
        if (actor == null || actor.getStore() == null) {
            throw new IllegalStateException("매장 소속 사용자만 결제를 진행할 수 있습니다.");
        }
        return actor.getStore();
    }

    private void verifyPaymentAccess(Payment payment, User actor) {
        if (actor == null || actor.getStore() == null) {
            return;
        }

        Long actorStoreId = actor.getStore().getId();
        if (actorStoreId != null && !actorStoreId.equals(payment.getStore().getId())) {
            throw new IllegalStateException("다른 매장의 결제에는 접근할 수 없습니다.");
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
            throw new IllegalStateException("다른 매장의 결제 목록에는 접근할 수 없습니다.");
        }
        return actorStoreId;
    }

    private PaymentStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return PaymentStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("유효하지 않은 결제 상태입니다. status=" + status);
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

    private String text(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private BigDecimal decimal(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return BigDecimal.ZERO;
        }
        if (value.isNumber()) {
            return value.decimalValue();
        }
        try {
            return new BigDecimal(value.asText());
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private record OrderSnapshotLine(Long productId, int qty, BigDecimal unitPrice) {
    }
}
