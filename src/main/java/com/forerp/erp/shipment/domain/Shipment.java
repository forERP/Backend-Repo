package com.forerp.erp.shipment.domain;

import com.forerp.erp.inbound.domain.Inbound;
import com.forerp.erp.outbound.domain.Outbound;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "shipments",
        indexes = {
                @Index(name = "idx_shipment_tracking", columnList = "carrier,tracking_number")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipment_id")
    private Long id;

    /* 출고 연관 (nullable) */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outbound_id", unique = true)
    private Outbound outbound;

    /* 입고 연관 (nullable) */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inbound_id", unique = true)
    private Inbound inbound;

    @Column(name = "carrier", length = 30)
    private String carrier;

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

    /* ===== 생성 (출고) ===== */
    public static Shipment createForOutbound(Outbound outbound) {
        Shipment shipment = new Shipment();
        shipment.outbound = outbound;
        shipment.status = ShipmentStatus.READY;
        shipment.createdAt = LocalDateTime.now();

        outbound.assignShipment(shipment);
        return shipment;
    }

    /* ===== 생성 (입고) ===== */
    public static Shipment createForInbound(Inbound inbound) {
        Shipment shipment = new Shipment();
        shipment.inbound = inbound;
        shipment.status = ShipmentStatus.READY;
        shipment.createdAt = LocalDateTime.now();

        inbound.assignShipment(shipment);
        return shipment;
    }

    /* ===== 배송 출발 ===== */
    public void depart(String carrier, String trackingNumber) {
        if (status != ShipmentStatus.READY) {
            throw new IllegalStateException("배송 출발이 불가능한 상태입니다.");
        }
        if (carrier == null || carrier.isBlank()) {
            throw new IllegalArgumentException("택배사는 필수입니다.");
        }
        if (trackingNumber == null || trackingNumber.isBlank()) {
            throw new IllegalArgumentException("송장번호는 필수입니다.");
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