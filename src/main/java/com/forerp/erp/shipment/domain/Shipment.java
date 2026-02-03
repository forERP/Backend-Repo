package com.forerp.erp.shipment.domain;

import com.forerp.erp.outbound.domain.Outbound;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipment_id")
    private Long id;

    /* 출고 연관 */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "outbound_id", nullable = false, unique = true)
    private Outbound outbound;

    /* 입고 연관 */
    // @OneToOne(fetch = FetchType.LAZY, optional = false)
    // @JoinColumn(name = "purchase_order_id", nullable = false)
    // private PurchaseOrder purchaseOrder;

    @Column(name = "carrier", length = 30)
    private String carrier;   // 택배사 코드 or 이름 (CJ, LOTTE, HANJIN 등)

    @Column(name = "tracking_number", length = 50)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentStatus status;

    @Column(name = "departed_at")
    private LocalDateTime departedAt;

    @Column(name = "arrived_at")
    private LocalDateTime arrivedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /* ===== 생성 로직 ===== */
    public static Shipment createForOutbound(Outbound outbound) {
        Shipment shipment = new Shipment();
        shipment.outbound = outbound;
        shipment.status = ShipmentStatus.READY;
        shipment.createdAt = LocalDateTime.now();

        outbound.assignShipment(shipment);
        return shipment;
    }

    /* ===== 배송 출발 ===== */
    public void depart(String carrier, String trackingNumber) {
        if (status != ShipmentStatus.READY) {
            throw new IllegalStateException("배송 출발이 불가능한 상태입니다.");
        }
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
        this.status = ShipmentStatus.SHIPPING;
        this.departedAt = LocalDateTime.now();
    }

    /* ===== 배송 도착 ===== */
    public void arrive() {
        if (status != ShipmentStatus.SHIPPING) {
            throw new IllegalStateException("배송 도착이 불가능한 상태입니다.");
        }
        this.status = ShipmentStatus.ARRIVED;
        this.arrivedAt = LocalDateTime.now();
    }
}