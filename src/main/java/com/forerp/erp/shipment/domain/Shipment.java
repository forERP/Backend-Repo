package com.forerp.erp.shipment.domain;

import com.forerp.erp.order.domain.Order;
import com.forerp.erp.store.domain.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shipment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "shipped_at", nullable = false)
    private LocalDateTime shippedAt;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShipmentItem> items = new ArrayList<>();

    /* ===== 생성 로직 ===== */
    public static Shipment create(Order order, Store store, List<ShipmentItem> items) {
        Shipment shipment = new Shipment();
        shipment.order = order;
        shipment.store = store;
        shipment.shippedAt = LocalDateTime.now();

        items.forEach(item -> {
            item.assignShipment(shipment);
            shipment.items.add(item);
        });

        return shipment;
    }
}