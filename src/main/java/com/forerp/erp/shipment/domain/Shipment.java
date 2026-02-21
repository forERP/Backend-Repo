package com.forerp.erp.shipment.domain;

import com.forerp.erp.inbound.domain.Inbound;
import com.forerp.erp.outbound.domain.Outbound;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "shipments",
        indexes = {
                @Index(name = "idx_shipment_tracking", columnList = "carrier,tracking_number"),
                @Index(name = "idx_shipment_tracking_code", columnList = "carrier_code,tracking_number")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipment_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outbound_id", unique = true)
    private Outbound outbound;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inbound_id", unique = true)
    private Inbound inbound;

    @Column(name = "carrier_code", length = 100)
    private String carrierCode;

    @Column(name = "carrier", length = 80)
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

    private void validateLink() {
        if ((inbound == null && outbound == null) || (inbound != null && outbound != null)) {
            throw new IllegalStateException("Shipment must be linked to either inbound or outbound.");
        }
    }

    public static Shipment createForOutbound(Outbound outbound) {
        Shipment shipment = new Shipment();
        shipment.outbound = outbound;
        shipment.validateLink();
        shipment.status = ShipmentStatus.READY;
        shipment.createdAt = LocalDateTime.now();

        outbound.assignShipment(shipment);
        return shipment;
    }

    public static Shipment createForInbound(Inbound inbound) {
        Shipment shipment = new Shipment();
        shipment.inbound = inbound;
        shipment.validateLink();
        shipment.status = ShipmentStatus.READY;
        shipment.createdAt = LocalDateTime.now();

        inbound.assignShipment(shipment);
        return shipment;
    }

    public void depart(String carrierCode, String carrier, String trackingNumber) {
        validateLink();
        if (status != ShipmentStatus.READY) {
            throw new IllegalStateException("Shipment cannot depart from the current status.");
        }
        if (carrier == null || carrier.isBlank()) {
            throw new IllegalArgumentException("carrier is required.");
        }
        if (trackingNumber == null || trackingNumber.isBlank()) {
            throw new IllegalArgumentException("trackingNumber is required.");
        }

        this.carrierCode = normalizeCarrierCode(carrierCode);
        this.carrier = carrier.trim();
        this.trackingNumber = trackingNumber.trim();
        this.status = ShipmentStatus.SHIPPING;
        this.departedAt = LocalDateTime.now();
    }

    public void arrive() {
        validateLink();
        if (status != ShipmentStatus.SHIPPING) {
            throw new IllegalStateException("Shipment cannot arrive from the current status.");
        }
        this.status = ShipmentStatus.ARRIVED;
        this.arrivedAt = LocalDateTime.now();
    }

    public void markShippingFromTracking() {
        validateLink();
        if (status == ShipmentStatus.READY) {
            this.status = ShipmentStatus.SHIPPING;
            if (this.departedAt == null) {
                this.departedAt = LocalDateTime.now();
            }
        }
    }

    public void markArrivedFromTracking() {
        validateLink();
        if (status == ShipmentStatus.SHIPPING) {
            this.status = ShipmentStatus.ARRIVED;
            if (this.arrivedAt == null) {
                this.arrivedAt = LocalDateTime.now();
            }
        }
    }

    private String normalizeCarrierCode(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
