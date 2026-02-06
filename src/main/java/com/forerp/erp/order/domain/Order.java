package com.forerp.erp.order.domain;

import com.forerp.erp.store.domain.Store;
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
    public static Order create(Store store, List<OrderItem> items) {
        Order order = new Order();
        order.store = store;
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