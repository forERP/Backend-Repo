package com.forerp.erp.store.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "store")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long storeId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "store_type", nullable = false, length = 10)
    private StoreType storeType = StoreType.STORE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StoreStatus status = StoreStatus.OPEN;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
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
}