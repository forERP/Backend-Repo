package com.forerp.erp.warehouse.domain;

import com.forerp.erp.store.domain.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "warehouses",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_warehouse_store_code",
                        columnNames = {"store_id", "code"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warehouse_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // 창고 코드 (지점 내 유니크) : MAIN, SUB, COLD, BACKROOM 등
    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    // 위치 정보 없어도 되나

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /* ===== 생성 로직 ===== */
    public static Warehouse create(
            Store store,
            String code,
            String name
    ) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("창고 코드는 필수입니다.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("창고 이름은 필수입니다.");
        }

        Warehouse warehouse = new Warehouse();
        warehouse.store = store;
        warehouse.code = code.trim().toUpperCase();
        warehouse.name = name.trim();
        warehouse.active = true;
        warehouse.createdAt = LocalDateTime.now();

        return warehouse;
    }

    /* ===== 상태 제어 ===== */
    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public void updateInfo(String code, String name, Boolean active) {
        if (code != null && !code.isBlank()) {
            this.code = code.trim().toUpperCase();
        }
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
        if (active != null) {
            this.active = active;
        }
    }
}