package com.forerp.erp.store.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long id;

    @Column(name = "store_code", nullable = false, length = 10, unique = true)
    private String storeCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "store_type", nullable = false)
    private StoreType storeType = StoreType.STORE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StoreStatus status = StoreStatus.OPEN;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    private Store(String name, StoreType storeType, StoreStatus status, String storeCode, String phone, String address) {
        this.name = name;
        this.storeCode = storeCode;
        this.storeType = (storeType == null) ? StoreType.STORE : storeType;
        this.status = (status == null) ? StoreStatus.OPEN : status;
        this.phone = phone;
        this.address = address;
    }

    public boolean isHQ() {
        return this.storeType == StoreType.HQ;
    }

    public boolean isActive() {
        return this.status == StoreStatus.OPEN;
    }

    public void close() {
        this.status = StoreStatus.CLOSED;
    }

    public void deactivate() {
        this.status = StoreStatus.INACTIVE;
    }

    public void open() {
        this.status = StoreStatus.OPEN;
    }

    public void updateInfo(String name, String phone, String address) {
        if (name != null) this.name = name;
        if (phone != null) this.phone = phone;
        if (address != null) this.address = address;
    }
}