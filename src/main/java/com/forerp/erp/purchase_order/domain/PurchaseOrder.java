package com.forerp.erp.purchase_order.domain;

import com.forerp.erp.purchase_req.domain.PurchaseRequest;
import com.forerp.erp.purchase_req.domain.PurchaseRequestStatus;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.supplier.domain.Supplier;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.warehouse.domain.Warehouse;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_request_id")
    private PurchaseRequest purchaseRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authored_by_user_id")
    private User authoredBy;

    @Column(length = 100)
    private String memo;

    @Column(name = "delivery_due_date")
    private LocalDate deliveryDueDate;

    @Column(name = "receiver_name", length = 100)
    private String receiverName;

    @Column(name = "receiver_phone", length = 30)
    private String receiverPhone;

    @Column(name = "shipping_address", length = 255)
    private String shippingAddress;

    @Column(name = "payment_terms", length = 100)
    private String paymentTerms;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseOrderStatus status;

    @Column(name = "ordered_at")
    private LocalDateTime orderedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "purchaseOrder",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PurchaseOrderItem> items = new ArrayList<>();

    public static PurchaseOrder createFromRequest(
            PurchaseRequest request,
            Supplier supplier,
            Store store,
            Warehouse warehouse,
            User authoredBy,
            LocalDate deliveryDueDate,
            String receiverName,
            String receiverPhone,
            String shippingAddress,
            String paymentTerms,
            String memo,
            List<PurchaseOrderItem> items
    ) {
        if (request.getStatus() != PurchaseRequestStatus.REQUESTED
                && request.getStatus() != PurchaseRequestStatus.APPROVED) {
            throw new IllegalStateException("요청 상태가 발주서 작성 가능한 상태가 아닙니다.");
        }

        PurchaseOrder po = new PurchaseOrder();
        po.purchaseRequest = request;
        po.supplier = supplier;
        po.store = store;
        po.warehouse = warehouse;
        po.authoredBy = authoredBy;
        po.deliveryDueDate = deliveryDueDate;
        po.receiverName = receiverName;
        po.receiverPhone = receiverPhone;
        po.shippingAddress = shippingAddress;
        po.paymentTerms = paymentTerms;
        po.memo = memo;
        po.status = PurchaseOrderStatus.CREATED;
        po.createdAt = LocalDateTime.now();

        items.forEach(item -> {
            item.assignPurchaseOrder(po);
            po.items.add(item);
        });

        return po;
    }

    public void order() {
        if (this.status != PurchaseOrderStatus.CREATED) {
            throw new IllegalStateException("발주 가능한 상태가 아닙니다.");
        }
        if (this.purchaseRequest != null && this.purchaseRequest.getStatus() != PurchaseRequestStatus.APPROVED) {
            throw new IllegalStateException("발주 요청 승인 이후에만 발주 확정이 가능합니다.");
        }
        this.status = PurchaseOrderStatus.ORDERED;
        this.orderedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status == PurchaseOrderStatus.ORDERED) {
            throw new IllegalStateException("이미 발주된 주문은 취소할 수 없습니다.");
        }
        this.status = PurchaseOrderStatus.CANCELED;
    }

    public void markReceived() {
        if (this.status != PurchaseOrderStatus.ORDERED) {
            throw new IllegalStateException("발주 상태가 ORDERED가 아닙니다.");
        }
        this.status = PurchaseOrderStatus.RECEIVED;
    }

    public void updateDraft(
            Supplier supplier,
            Warehouse warehouse,
            LocalDate deliveryDueDate,
            String receiverName,
            String receiverPhone,
            String shippingAddress,
            String paymentTerms,
            String memo
    ) {
        if (this.status != PurchaseOrderStatus.CREATED) {
            throw new IllegalStateException("작성 단계(CREATED) 발주서만 수정할 수 있습니다.");
        }
        if (this.purchaseRequest != null && this.purchaseRequest.getStatus() != PurchaseRequestStatus.REQUESTED) {
            throw new IllegalStateException("요청 승인 이후에는 발주서를 수정할 수 없습니다.");
        }

        this.supplier = supplier;
        this.warehouse = warehouse;
        this.deliveryDueDate = deliveryDueDate;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.shippingAddress = shippingAddress;
        this.paymentTerms = paymentTerms;
        this.memo = memo;
    }
}