package com.forerp.erp.discard;

import com.forerp.erp.store.domain.Store;
import com.forerp.erp.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

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

    protected Discard() {
    }

    public static Discard create(
            Store store,
            String reason,
            User actor
    ) {
        Discard discard = new Discard();
        discard.store = store;
        discard.reason = reason;
        discard.createdBy = actor;
        discard.discardedAt = LocalDateTime.now();
        return discard;
    }
}