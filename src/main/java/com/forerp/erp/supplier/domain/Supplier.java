package com.forerp.erp.supplier.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplier_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String contactName;

    @Column(length = 30)
    private String contactPhone;

    @Column(length = 100)
    private String contactEmail;

    @Column(length = 200)
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /* ===== 생성 ===== */
    public static Supplier create(
            String name,
            String contactName,
            String contactPhone,
            String contactEmail,
            String address,
            Double latitude,
            Double longitude
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("거래처명은 필수입니다.");
        }

        Supplier supplier = new Supplier();
        supplier.name = name.trim();
        supplier.contactName = normalize(contactName);
        supplier.contactPhone = normalize(contactPhone);
        supplier.contactEmail = normalize(contactEmail);
        supplier.address = normalize(address);
        supplier.latitude = latitude;
        supplier.longitude = longitude;
        return supplier;
    }

    public void update(
            String name,
            String contactName,
            String contactPhone,
            String contactEmail,
            String address,
            Double latitude,
            Double longitude,
            Boolean active
    ) {
        if (name != null) {
            if (name.isBlank()) {
                throw new IllegalArgumentException("거래처명은 비워둘 수 없습니다.");
            }
            this.name = name.trim();
        }

        this.contactName = normalize(contactName);
        this.contactPhone = normalize(contactPhone);
        this.contactEmail = normalize(contactEmail);
        this.address = normalize(address);
        if (latitude != null || address != null) {
            this.latitude = latitude;
        }
        if (longitude != null || address != null) {
            this.longitude = longitude;
        }

        if (active != null) {
            this.active = active;
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
