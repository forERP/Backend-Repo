package com.forerp.erp.order.domain;

import com.forerp.erp.store.domain.Store;
import com.forerp.erp.warehouse.domain.Warehouse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    /* ===== 생성 ===== */
    public static Order create(Store store, Warehouse warehouse, List<OrderItem> items) {
        Order order = new Order();
        order.store = store;
        order.warehouse = warehouse;
        order.status = OrderStatus.PLACED;
        order.orderedAt = LocalDateTime.now();

        items.forEach(item -> {
            item.assignOrder(order);
            order.items.add(item);
        });

        order.totalAmount = items.stream()
                .map(OrderItem::calculateAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return order;
    }

    public BigDecimal cancelPlacedItems(List<OrderItemCancelCommand> cancelCommands) {
        if (this.status != OrderStatus.PLACED) {
            throw new IllegalStateException("부분 취소는 PLACED 상태에서만 가능합니다.");
        }

        if (cancelCommands == null || cancelCommands.isEmpty()) {
            throw new IllegalArgumentException("취소할 주문 항목이 비어 있습니다.");
        }

        BigDecimal canceledAmount = BigDecimal.ZERO;

        for (OrderItemCancelCommand command : cancelCommands) {
            OrderItem orderItem = this.items.stream()
                    .filter(item -> item.getId().equals(command.orderItemId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "주문 항목을 찾을 수 없습니다. orderItemId=" + command.orderItemId()
                    ));

            canceledAmount = canceledAmount.add(orderItem.reduceQuantity(command.qty()));
        }

        this.items.removeIf(item -> item.getQuantity() <= 0);
        recalculateTotalAmount();

        if (this.items.isEmpty() || this.totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            this.status = OrderStatus.CANCELED;
            this.totalAmount = BigDecimal.ZERO;
        }

        return canceledAmount;
    }

    public void markCanceledByRefund() {
        if (this.status == OrderStatus.CANCELED) {
            return;
        }
        this.status = OrderStatus.CANCELED;
    }

    private void recalculateTotalAmount() {
        this.totalAmount = this.items.stream()
                .map(OrderItem::calculateAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public record OrderItemCancelCommand(Long orderItemId, int qty) {
        public OrderItemCancelCommand {
            if (orderItemId == null) {
                throw new IllegalArgumentException("orderItemId는 필수입니다.");
            }
            if (qty <= 0) {
                throw new IllegalArgumentException("취소 수량은 1 이상이어야 합니다.");
            }
        }
    }

    /* ===== 준비 완료 ===== */
    public void markPrepared() {
        if (this.status != OrderStatus.PLACED) {
            throw new IllegalStateException("준비 완료 가능한 주문 상태가 아닙니다.");
        }
        this.status = OrderStatus.PREPARED;
    }

    /* ===== 배송 출발 ===== */
    public void markShipped() {
        if (this.status != OrderStatus.PREPARED) {
            throw new IllegalStateException("배송 출발 가능한 주문 상태가 아닙니다.");
        }
        this.status = OrderStatus.SHIPPED;
    }

    /* ===== 배송 도착 ===== */
    public void markArrived() {
        if (this.status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("도착 처리 가능한 주문 상태가 아닙니다.");
        }
        this.status = OrderStatus.ARRIVED;
    }

    /* ===== 취소 ===== */
    public void cancel() {
        if (this.status != OrderStatus.PLACED) {
            throw new IllegalStateException("확정 이후 단계의 주문은 취소할 수 없습니다.");
        }
        this.status = OrderStatus.CANCELED;
    }
}
