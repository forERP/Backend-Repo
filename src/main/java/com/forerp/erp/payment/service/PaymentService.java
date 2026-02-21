package com.forerp.erp.payment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forerp.erp.inventory.domain.InventoryHistory;
import com.forerp.erp.inventory.domain.RefType;
import com.forerp.erp.inventory.repository.InventoryHistoryRepository;
import com.forerp.erp.order.domain.Order;
import com.forerp.erp.order.domain.OrderItem;
import com.forerp.erp.order.domain.OrderItemComponent;
import com.forerp.erp.order.domain.OrderStatus;
import com.forerp.erp.order.dto.OrderResponse;
import com.forerp.erp.order.repository.OrderItemComponentRepository;
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
import com.forerp.erp.product.domain.ProductBundle;
import com.forerp.erp.product.domain.Product;
import com.forerp.erp.product.repository.ProductBundleRepository;
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
import java.util.Set;
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
    private final OrderItemComponentRepository orderItemComponentRepository;
    private final OutboundRepository outboundRepository;
    private final ProductRepository productRepository;
    private final ProductBundleRepository productBundleRepository;
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
                .orElseThrow(() -> new IllegalArgumentException("?롪퍒???繞벿뮻???筌먲퐢沅??嶺뚢돦堉??????怨룸????덈펲."));
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
            throw new IllegalStateException("?獄????띠럾??繞③뇡??롪퍒?????⑤객臾뜻뤆?쎛 ?熬곣뫀六???덈펲. status=" + payment.getStatus());
        }

        if (payment.getAmount().compareTo(request.getAmount()) != 0) {
            throw new IllegalArgumentException("?롪퍒????ル?녽뇡????源딅뭵??? ???용????덈펲.");
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
                    "?????곌떠????됰さ???낅슣?뽪룇 ??諛댁뎽?????덉넮??琉우뿰 ???吏????쳛???琉????鍮??",
                    request.getAmount()
            );
            payment.markCanceled(request.getAmount(), text(cancelResult, "status"), cancelResult.toString());
            throw new IllegalStateException("?롪퍒?????獄????琉?嶺뚯솘?嶺??????????怨쀬Ŧ ?낅슣?뽪룇 ??諛댁뎽?????덉넮??琉우뿰 ???吏????쳛???琉????鍮??", ex);
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
        return cancelInternal(actor, paymentId, request, false);
    }

    public PaymentCancelResponse cancelForReturn(User actor, Long paymentId, PaymentCancelRequest request) {
        return cancelInternal(actor, paymentId, request, true);
    }

    private PaymentCancelResponse cancelInternal(
            User actor,
            Long paymentId,
            PaymentCancelRequest request,
            boolean fromReturnProcess
    ) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("?롪퍒??節뉖ご?嶺뚢돦堉??????怨룸????덈펲."));
        verifyPaymentAccess(payment, actor);

        if (payment.getPaymentKey() == null || payment.getPaymentKey().isBlank()) {
            throw new IllegalStateException("?熬곣뫗異??獄????? ??? ?롪퍒?????낅퉵??");
        }

        if (payment.getOrder() == null) {
            throw new IllegalStateException("??⑤슡????낅슣?뽪룇 ?筌먲퐢沅뽪뤆?쎛 ??怨룸????덈펲.");
        }

        Order order = payment.getOrder();
        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("???? ???쳛????낅슣?뽪룇???낅퉵??");
        }

        Map<Long, Integer> cancelMap = normalizeCancelItems(request, order);
        boolean fullCancel = isFullCancel(cancelMap, order);
        validateCancelPolicy(order.getStatus(), fullCancel, fromReturnProcess);

        BigDecimal cancelAmount = computeCancelAmount(order, cancelMap);
        if (cancelAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("???쳛???ル?녽뇡??0 ??袁⑤┃???낅퉵??");
        }
        if (payment.getRemainingCancelableAmount().compareTo(cancelAmount) < 0) {
            throw new IllegalStateException("???쳛???띠럾????ル?녽뇡???貫?????곕????덈펲.");
        }

        Outbound outbound = outboundRepository.findByOrder_Id(order.getId()).orElse(null);
        boolean discardStock = Boolean.TRUE.equals(request.getDiscardStock());
        preValidateStockAdjustment(order, outbound, discardStock, cancelMap);

        JsonNode cancelResult = tossPaymentsClient.cancel(
                payment.getPaymentKey(),
                request.getReason(),
                cancelAmount
        );

        String orderStatusBefore = order.getStatus().name();
        boolean stockRefunded = false;

        if (order.getStatus() == OrderStatus.PLACED) {
            if (discardStock) {
                applyDiscardStockForPlaced(order, cancelMap, actor);
            }
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
            throw new IllegalStateException("嶺뚯솘???믨퀡由?춯?뼿 ???낅츎 ?낅슣?뽪룇 ??⑤객臾???낅퉵?? status=" + order.getStatus());
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
        if (("PLACED".equals(orderStatusBefore) && discardStock)
                || ("PREPARED".equals(orderStatusBefore) && discardStock)
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
                .orElseThrow(() -> new IllegalArgumentException("?롪퍒??節뉖ご?嶺뚢돦堉??????怨룸????덈펲."));
        verifyPaymentAccess(payment, actor);
        return PaymentSummaryResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PaymentSummaryResponse getByOrderId(User actor, Long orderId) {
        Payment payment = paymentRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new IllegalArgumentException("?낅슣?뽪룇????⑤슡????롪퍒??節뉖ご?嶺뚢돦堉??????怨룸????덈펲."));
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
            throw new IllegalArgumentException("?獄?낯??payload ???堉?????덉넮???곕????덈펲.");
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

        Set<Long> orderedProductIds = snapshotLines.stream()
                .map(OrderSnapshotLine::productId)
                .collect(Collectors.toSet());
        Map<Long, ProductBundle> bundleByProductId = findBundlesByProductIds(orderedProductIds);

        Set<Long> relatedProductIds = snapshotLines.stream()
                .flatMap(line -> resolveSnapshotDeductions(line, bundleByProductId).stream())
                .map(OrderSnapshotStockDeduction::productId)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        relatedProductIds.addAll(orderedProductIds);

        Map<Long, Product> productMap = productRepository.findAllById(relatedProductIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));
        List<OrderItem> orderItems = snapshotLines.stream()
                .map(line -> {
                    Product product = productMap.get(line.productId());
                    if (product == null) {
                        throw new IllegalStateException("상품을 찾을 수 없습니다. productId=" + line.productId());
                    }
                    OrderItem orderItem = OrderItem.create(product, line.qty(), line.unitPrice());
                    List<OrderSnapshotStockDeduction> stockDeductions = resolveSnapshotDeductions(line, bundleByProductId);
                    for (OrderSnapshotStockDeduction stockDeduction : stockDeductions) {
                        Product componentProduct = productMap.get(stockDeduction.productId());
                        if (componentProduct == null) {
                            throw new IllegalStateException("구성 상품을 찾을 수 없습니다. productId=" + stockDeduction.productId());
                        }
                        orderItem.addComponent(componentProduct, stockDeduction.qtyPerUnit());
                    }
                    return orderItem;
                })
                .toList();

        Order order = Order.create(
                payment.getStore(),
                warehouse,
                orderItems
        );
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
            throw new IllegalStateException("??뽮쉐 筌≪럡?у첎? ??곷뮸??덈뼄.");
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

        Set<Long> orderedProductIds = lines.stream()
                .map(OrderSnapshotLine::productId)
                .collect(Collectors.toSet());
        Map<Long, ProductBundle> bundleByProductId = findBundlesByProductIds(orderedProductIds);

        Set<Long> stockProductIds = new java.util.LinkedHashSet<>(orderedProductIds);
        lines.forEach(line -> resolveSnapshotDeductions(line, bundleByProductId)
                .forEach(stockDeduction -> stockProductIds.add(stockDeduction.productId())));

        for (Warehouse candidate : candidates) {
            List<StoreProduct> stockList = storeProductRepository.findByStore_IdAndWarehouse_IdAndProduct_IdIn(
                    store.getId(),
                    candidate.getId(),
                    new ArrayList<>(stockProductIds)
            );
            Map<Long, StoreProduct> stockMap = stockList.stream()
                    .collect(Collectors.toMap(sp -> sp.getProduct().getId(), sp -> sp));

            Map<Long, Integer> requiredStockQtyByProductId = new HashMap<>();
            boolean match = true;
            for (OrderSnapshotLine line : lines) {
                StoreProduct sp = stockMap.get(line.productId());
                if (sp == null || sp.getSaleStatus() != com.forerp.erp.storeproduct.domain.SaleStatus.ON) {
                    match = false;
                    break;
                }

                BigDecimal currentPrice = resolveUnitPrice(sp);
                if (currentPrice.compareTo(line.unitPrice()) != 0) {
                    match = false;
                    break;
                }

                List<OrderSnapshotStockDeduction> stockDeductions = resolveSnapshotDeductions(line, bundleByProductId);
                for (OrderSnapshotStockDeduction stockDeduction : stockDeductions) {
                    int requiredQty = stockDeduction.qtyPerUnit() * line.qty();
                    requiredStockQtyByProductId.merge(stockDeduction.productId(), requiredQty, Integer::sum);
                }
            }

            if (match && hasSufficientStock(stockMap, requiredStockQtyByProductId)) {
                return candidate;
            }
        }

        throw new IllegalStateException("野껉퀣??疫뀀뜆釉멩???깊뒄??롫뮉 ???х몴?筌≪뼚??????곷뮸??덈뼄.");
    }

    private Map<Long, ProductBundle> findBundlesByProductIds(Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        return productBundleRepository.findByProduct_IdIn(productIds).stream()
                .collect(Collectors.toMap(bundle -> bundle.getProduct().getId(), bundle -> bundle));
    }

    private List<OrderSnapshotStockDeduction> resolveSnapshotDeductions(
            OrderSnapshotLine line,
            Map<Long, ProductBundle> bundleByProductId
    ) {
        if (line.stockDeductions() != null && !line.stockDeductions().isEmpty()) {
            return line.stockDeductions().stream()
                    .filter(deduction -> deduction.productId() != null && deduction.qtyPerUnit() > 0)
                    .toList();
        }

        ProductBundle bundle = bundleByProductId.get(line.productId());
        if (bundle != null && bundle.getItems() != null && !bundle.getItems().isEmpty()) {
            return bundle.getItems().stream()
                    .map(item -> new OrderSnapshotStockDeduction(
                            item.getComponentProduct().getId(),
                            item.getQuantityPerBundle()
                    ))
                    .toList();
        }

        return List.of(new OrderSnapshotStockDeduction(line.productId(), 1));
    }

    private boolean hasSufficientStock(
            Map<Long, StoreProduct> stockMap,
            Map<Long, Integer> requiredStockQtyByProductId
    ) {
        for (Map.Entry<Long, Integer> entry : requiredStockQtyByProductId.entrySet()) {
            StoreProduct stock = stockMap.get(entry.getKey());
            if (stock == null || stock.getQuantity() < entry.getValue()) {
                return false;
            }
        }
        return true;
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

    private void preValidateStockAdjustment(
            Order order,
            Outbound outbound,
            boolean discardStock,
            Map<Long, Integer> cancelMap
    ) {
        if (!discardStock) {
            return;
        }

        if (order.getStatus() == OrderStatus.PLACED) {
            Map<Long, Integer> requiredQtyByProductId = resolveRequiredComponentQty(order, cancelMap);
            validateStockAvailability(order, requiredQtyByProductId);
            return;
        }

        if (order.getStatus() != OrderStatus.PREPARED) {
            return;
        }

        if (outbound != null) {
            for (OutboundItem item : outbound.getItems()) {
                if (item.getStoreProduct().getQuantity() < item.getQuantity()) {
                    throw new IllegalStateException("?癒?┛ 筌ｌ꼶??????у첎? ?봔鈺곌퉲鍮??덈뼄.");
                }
            }
            return;
        }

        Map<Long, Integer> fullOrderItemQtyMap = buildOrderItemQtyMap(order.getItems());
        Map<Long, Integer> requiredQtyByProductId = resolveRequiredComponentQty(order, fullOrderItemQtyMap);
        validateStockAvailability(order, requiredQtyByProductId);
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

        Map<Long, Integer> requiredQtyByProductId = resolveRequiredComponentQty(
                order,
                buildOrderItemQtyMap(order.getItems())
        );
        applyDiscardStockByRequirement(order, requiredQtyByProductId, actor);
    }

    private void applyDiscardStockForPlaced(Order order, Map<Long, Integer> cancelMap, User actor) {
        Map<Long, Integer> requiredQtyByProductId = resolveRequiredComponentQty(order, cancelMap);
        applyDiscardStockByRequirement(order, requiredQtyByProductId, actor);
    }

    private Map<Long, Integer> buildOrderItemQtyMap(List<OrderItem> orderItems) {
        Map<Long, Integer> orderItemQtyMap = new LinkedHashMap<>();
        for (OrderItem orderItem : orderItems) {
            orderItemQtyMap.put(orderItem.getId(), orderItem.getQuantity());
        }
        return orderItemQtyMap;
    }

    private Map<Long, Integer> resolveRequiredComponentQty(Order order, Map<Long, Integer> orderItemQtyMap) {
        if (orderItemQtyMap == null || orderItemQtyMap.isEmpty()) {
            return Map.of();
        }

        Map<Long, OrderItem> orderItemById = order.getItems().stream()
                .collect(Collectors.toMap(OrderItem::getId, item -> item));

        List<OrderItemComponent> orderItemComponents = orderItemComponentRepository
                .findByOrderItem_IdIn(orderItemQtyMap.keySet());
        Map<Long, List<OrderItemComponent>> componentsByOrderItemId = orderItemComponents.stream()
                .collect(Collectors.groupingBy(component -> component.getOrderItem().getId()));

        Map<Long, Integer> requiredQtyByProductId = new LinkedHashMap<>();

        for (Map.Entry<Long, Integer> entry : orderItemQtyMap.entrySet()) {
            Long orderItemId = entry.getKey();
            Integer orderItemQty = entry.getValue();

            OrderItem orderItem = orderItemById.get(orderItemId);
            if (orderItem == null) {
                throw new IllegalStateException("雅뚯눖揆 ?????筌≪뼚??????곷뮸??덈뼄. orderItemId=" + orderItemId);
            }

            if (orderItemQty == null || orderItemQty <= 0) {
                continue;
            }

            List<OrderItemComponent> components = componentsByOrderItemId.get(orderItemId);
            if (components == null || components.isEmpty()) {
                requiredQtyByProductId.merge(orderItem.getProduct().getId(), orderItemQty, Integer::sum);
                continue;
            }

            for (OrderItemComponent component : components) {
                int requiredQty = orderItemQty * component.getQuantityPerOrderItem();
                requiredQtyByProductId.merge(component.getComponentProduct().getId(), requiredQty, Integer::sum);
            }
        }

        return requiredQtyByProductId;
    }

    private void validateStockAvailability(Order order, Map<Long, Integer> requiredQtyByProductId) {
        for (Map.Entry<Long, Integer> entry : requiredQtyByProductId.entrySet()) {
            StoreProduct stock = storeProductRepository
                    .findByStore_IdAndWarehouse_IdAndProduct_Id(
                            order.getStore().getId(),
                            order.getWarehouse().getId(),
                            entry.getKey()
                    )
                    .orElseThrow(() -> new IllegalStateException("?????類ｋ궖??筌≪뼚??????곷뮸??덈뼄. productId=" + entry.getKey()));

            if (stock.getQuantity() < entry.getValue()) {
                throw new IllegalStateException("?癒?┛ 筌ｌ꼶??????у첎? ?봔鈺곌퉲鍮??덈뼄.");
            }
        }
    }

    private void applyDiscardStockByRequirement(Order order, Map<Long, Integer> requiredQtyByProductId, User actor) {
        for (Map.Entry<Long, Integer> entry : requiredQtyByProductId.entrySet()) {
            StoreProduct stock = storeProductRepository
                    .findByStore_IdAndWarehouse_IdAndProduct_Id(
                            order.getStore().getId(),
                            order.getWarehouse().getId(),
                            entry.getKey()
                    )
                    .orElseThrow(() -> new IllegalStateException("?????類ｋ궖??筌≪뼚??????곷뮸??덈뼄. productId=" + entry.getKey()));

            InventoryHistory history = stock.decreaseStock(
                    entry.getValue(),
                    RefType.DISCARD,
                    order.getId(),
                    null,
                    actor
            );
            inventoryHistoryRepository.save(history);
        }
    }

    private void applyReturnStock(Order order, Outbound outbound, User actor) {
        if (outbound == null) {
            throw new IllegalStateException("?怨쀫츇???筌먲퐢沅뽪뤆?쎛 ??怨룹꽑 ?????곌랜踰??嶺뚳퐣瑗?怨?ご???????怨룸????덈펲.");
        }
        if (!(outbound.getStatus() == OutboundStatus.CONFIRMED || outbound.getStatus() == OutboundStatus.ARRIVED)) {
            throw new IllegalStateException("?????곌랜踰??? ?怨쀫츇???筌먦끉????袁⑸쐩 ??⑤객臾????ｇ춯??띠럾??繞③뜮????덈펲.");
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
                throw new IllegalArgumentException("?낅슣?뽪룇 ?????嶺뚢돦堉??????怨룸????덈펲. orderItemId=" + item.getOrderItemId());
            }

            int mergedQty = normalized.getOrDefault(item.getOrderItemId(), 0) + item.getQty();
            if (mergedQty > orderedQty) {
                throw new IllegalArgumentException("???쳛????濡?럸???낅슣?뽪룇 ??濡?럸???貫?????곕????덈펲. orderItemId=" + item.getOrderItemId());
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

    private void validateCancelPolicy(OrderStatus orderStatus, boolean fullCancel, boolean fromReturnProcess) {
        if (orderStatus == OrderStatus.PLACED) {
            return;
        }

        if (!fullCancel) {
            throw new IllegalStateException("?遊붋?????쳛???PLACED ??⑤객臾????ｇ춯??띠럾??繞③뜮????덈펲.");
        }

        if (orderStatus == OrderStatus.PREPARED) {
            return;
        }

        if (orderStatus == OrderStatus.SHIPPED || orderStatus == OrderStatus.ARRIVED) {
            if (fromReturnProcess) {
                return;
            }
            throw new IllegalStateException("?怨쀫츇????袁⑸쐩 ?낅슣?뽪룇?? ?꾩룇瑗배맱?API????????낅슣?섋땻??");
        }

        throw new IllegalStateException("???쳛???띠럾??繞③뇡??낅슣?뽪룇 ??⑤객臾뜻뤆?쎛 ?熬곣뫀六???덈펲. status=" + orderStatus);
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
                    .map(line -> new OrderSnapshotLine(
                            line.productId(),
                            line.qty(),
                            line.unitPrice(),
                            line.stockDeductions() == null
                                    ? List.of()
                                    : line.stockDeductions().stream()
                                    .map(deduction -> new OrderSnapshotStockDeduction(
                                            deduction.productId(),
                                            deduction.qtyPerUnit()
                                    ))
                                    .toList()
                    ))
                    .toList();
            return objectMapper.writeValueAsString(snapshotLines);
        } catch (Exception ex) {
            throw new IllegalStateException("?낅슣?뽪룇 ???고돩??嶺뚯쉳????븐슜?????덉넮???곕????덈펲.", ex);
        }
    }

    private List<OrderSnapshotLine> readOrderSnapshot(String snapshot) {
        try {
            return objectMapper.readValue(snapshot, SNAPSHOT_LINE_LIST_TYPE);
        } catch (Exception ex) {
            throw new IllegalStateException("?낅슣?뽪룇 ???고돩?????堉?????덉넮???곕????덈펲.", ex);
        }
    }

    private String buildOrderName(List<OrderStockAllocator.AllocatedLine> lines) {
        if (lines == null || lines.isEmpty()) {
            return "Product order";
        }

        String firstName = lines.get(0).productName();
        if (lines.size() == 1) {
            return firstName;
        }
        return firstName + " + " + (lines.size() - 1) + " items";
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
            throw new IllegalStateException("嶺뚮씞??????爰???????異??롪퍒??節뉖ご?嶺뚯쉳?듸쭛???????곕????덈펲.");
        }
        return actor.getStore();
    }

    private void verifyPaymentAccess(Payment payment, User actor) {
        if (actor == null || actor.getStore() == null) {
            return;
        }

        Long actorStoreId = actor.getStore().getId();
        if (actorStoreId != null && !actorStoreId.equals(payment.getStore().getId())) {
            throw new IllegalStateException("???섎?嶺뚮씞?????롪퍒?????裕???얜∥???????怨룸????덈펲.");
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
            throw new IllegalStateException("???섎?嶺뚮씞?????롪퍒???嶺뚮ㅄ維뽨빳???裕???얜∥???????怨룸????덈펲.");
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
            throw new IllegalArgumentException("??ル쪇???? ??? ?롪퍒?????⑤객臾???낅퉵?? status=" + status);
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

    private record OrderSnapshotLine(
            Long productId,
            int qty,
            BigDecimal unitPrice,
            List<OrderSnapshotStockDeduction> stockDeductions
    ) {
    }

    private record OrderSnapshotStockDeduction(Long productId, int qtyPerUnit) {
    }

}


