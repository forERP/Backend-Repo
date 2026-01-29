package com.forerp.erp.shipment.domain;

import com.forerp.erp.order.domain.OrderItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shipment_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShipmentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipment_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(nullable = false)
    private int quantity;

    /* ===== 생성 로직 ===== */
    public static ShipmentItem create(OrderItem orderItem, int quantity) {
        ShipmentItem item = new ShipmentItem();
        item.orderItem = orderItem;
        item.quantity = quantity;
        return item;
    }

    void assignShipment(Shipment shipment) {
        this.shipment = shipment;
    }
}