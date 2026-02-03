package com.forerp.erp.purchase_req.domain;

import com.forerp.erp.store.domain.Store;
import com.forerp.erp.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_request_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by")
    private User requestedBy;

    @Column(length = 100)
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseRequestStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "purchaseRequest",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PurchaseRequestItem> items = new ArrayList<>();

    /* ===== 생성 ===== */
    public static PurchaseRequest create(
            Store store,
            User requestedBy,
            String memo,
            List<PurchaseRequestItem> items
    ) {
        PurchaseRequest pr = new PurchaseRequest();
        pr.store = store;
        pr.requestedBy = requestedBy;
        pr.memo = memo;
        pr.status = PurchaseRequestStatus.REQUESTED;
        pr.createdAt = LocalDateTime.now();

        items.forEach(item -> {
            item.assignPurchaseRequest(pr);
            pr.items.add(item);
        });

        return pr;
    }

    /* ===== 승인 ===== */
    public void approve() {
        if (this.status != PurchaseRequestStatus.REQUESTED) {
            throw new IllegalStateException("요청 상태가 아닙니다.");
        }
        this.status = PurchaseRequestStatus.APPROVED;
    }

    /* ===== 반려 ===== */
    public void reject() {
        if (this.status != PurchaseRequestStatus.REQUESTED) {
            throw new IllegalStateException("요청 상태가 아닙니다.");
        }
        this.status = PurchaseRequestStatus.REJECTED;
    }
}