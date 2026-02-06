package com.forerp.erp.discard.domain;

import com.forerp.erp.store.domain.Store;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.warehouse.domain.Warehouse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "discards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Discard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discard_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(length = 30)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "discarded_at")
    private LocalDateTime discardedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "discard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiscardItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscardStatus status;

    /* ===== 생성 로직 ===== */
    public static Discard create(Store store, Warehouse warehouse, String reason, User actor, List<DiscardItem> items) {
        Discard discard = new Discard();
        discard.store = store;
        discard.warehouse = warehouse;
        discard.reason = reason;
        discard.createdBy = actor;

        discard.status = DiscardStatus.CREATED;
        discard.createdAt = LocalDateTime.now();
        discard.discardedAt = null;

        if (items != null) {
            items.forEach(item -> {
                item.assignDiscard(discard);
                discard.items.add(item);
            });
        }
        return discard;
    }

    /* ===== 확정 로직 ===== */
    public void confirm() {
        if (this.status == DiscardStatus.CONFIRMED) {
            throw new IllegalStateException("이미 확정된 폐기입니다.");
        }
        if (this.status == DiscardStatus.CANCELED) {
            throw new IllegalStateException("취소된 폐기는 확정할 수 없습니다.");
        }

        this.status = DiscardStatus.CONFIRMED;
        this.discardedAt = LocalDateTime.now();
    }

    /* ===== 취소 로직 ===== */
    public void cancel() {
        if (this.status != DiscardStatus.CREATED) {
            throw new IllegalStateException("확정/취소된 폐기는 취소할 수 없습니다.");
        }
        this.status = DiscardStatus.CANCELED;
    }

    /* ===== 가드 메서드 ===== */
    public void requireCreated() {
        if (this.status != DiscardStatus.CREATED) {
            throw new IllegalStateException("CREATED 상태가 아닙니다.");
        }
    }

    public void requireNotCanceled() {
        if (this.status == DiscardStatus.CANCELED) {
            throw new IllegalStateException("취소된 폐기입니다.");
        }
    }
}