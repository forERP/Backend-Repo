package com.forerp.erp.discard.domain;

import com.forerp.erp.store.domain.Store;
import com.forerp.erp.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "discards")
@Getter
public class Discard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discard_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(length = 30)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "discarded_at", nullable = false)
    private LocalDateTime discardedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "discard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiscardItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscardStatus status;

    protected Discard() {
    }

    /* ===== 생성 로직 ===== */
    public static Discard create(
            Store store,
            String reason,
            User actor,
            List<DiscardItem> items
    ) {
        Discard discard = new Discard();
        discard.store = store;
        discard.reason = reason;
        discard.createdBy = actor;
        discard.discardedAt = LocalDateTime.now();
        discard.status = DiscardStatus.CREATED;

        items.forEach(item -> {
            item.assignDiscard(discard);
            discard.items.add(item);
        });

        return discard;
    }

    /* ===== 확정 로직 ===== */
    public void confirm() {
        if (this.status == DiscardStatus.CONFIRMED) {
            throw new IllegalStateException("이미 확정된 폐기입니다.");
        }
        this.status = DiscardStatus.CONFIRMED;
    }
}