package com.forerp.erp.outbound.domain;

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
@Table(name = "outbounds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Outbound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbound_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "outbound", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OutboundItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboundStatus status;

    /* ===== 생성 로직 ===== */
    public static Outbound create(Order order, Store store, List<OutboundItem> items) {
        Outbound outbound = new Outbound();
        outbound.order = order;
        outbound.store = store;
        outbound.createdAt = LocalDateTime.now();
        outbound.status = OutboundStatus.CREATED;

        items.forEach(item -> {
            item.assignOutbound(outbound);
            outbound.items.add(item);
        });

        return outbound;
    }

    /* ===== 확정 로직 ===== */
    public void confirm() {
        if (this.status == OutboundStatus.CONFIRMED) {
            throw new IllegalStateException("이미 확정된 출고입니다.");
        }
        this.status = OutboundStatus.CONFIRMED;
    }
}