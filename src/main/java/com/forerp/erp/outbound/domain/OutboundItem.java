package com.forerp.erp.outbound.domain;

import com.forerp.erp.order.domain.OrderItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "outbound_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboundItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbound_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outbound_id", nullable = false)
    private Outbound outbound;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(nullable = false)
    private int quantity;

    /* ===== 생성 로직 ===== */
    public static OutboundItem create(OrderItem orderItem, int quantity) {
        OutboundItem item = new OutboundItem();
        item.orderItem = orderItem;
        item.quantity = quantity;
        return item;
    }

    void assignOutbound(Outbound outbound) {
        this.outbound = outbound;
    }
}