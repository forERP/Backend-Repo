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
            String address
    ) {
        Supplier supplier = new Supplier();
        supplier.name = name;
        supplier.contactName = contactName;
        supplier.contactPhone = contactPhone;
        supplier.contactEmail = contactEmail;
        supplier.address = address;
        return supplier;
    }
}