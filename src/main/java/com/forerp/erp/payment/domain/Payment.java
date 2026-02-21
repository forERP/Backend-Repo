package com.forerp.erp.payment.domain;

import com.forerp.erp.order.domain.Order;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.warehouse.domain.Warehouse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_merchant_order_id", columnNames = "merchant_order_id"),
                @UniqueConstraint(name = "uk_payment_payment_key", columnNames = "payment_key")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_mode", nullable = false, length = 30)
    private ServiceMode serviceMode;

    @Column(name = "merchant_order_id", nullable = false, length = 64)
    private String merchantOrderId;

    @Column(name = "customer_key", nullable = false, length = 64)
    private String customerKey;

    @Column(name = "order_name", nullable = false, length = 200)
    private String orderName;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "approved_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "canceled_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal canceledAmount;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "payment_key", length = 200)
    private String paymentKey;

    @Column(name = "method", length = 50)
    private String method;

    @Column(name = "toss_status", length = 50)
    private String tossStatus;

    @Column(name = "fail_code", length = 100)
    private String failCode;

    @Column(name = "fail_message", length = 500)
    private String failMessage;

    @Lob
    @Column(name = "raw_response", columnDefinition = "LONGTEXT")
    private String rawResponse;

    @Lob
    @Column(name = "order_snapshot", columnDefinition = "LONGTEXT")
    private String orderSnapshot;

    @Column(name = "prepared_at", nullable = false)
    private LocalDateTime preparedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static Payment prepare(
            Store store,
            String merchantOrderId,
            String customerKey,
            String orderName,
            BigDecimal amount,
            ServiceMode serviceMode,
            String orderSnapshot
    ) {
        Payment payment = new Payment();
        payment.store = store;
        payment.status = PaymentStatus.READY;
        payment.serviceMode = serviceMode;
        payment.merchantOrderId = merchantOrderId;
        payment.customerKey = customerKey;
        payment.orderName = orderName;
        payment.amount = amount;
        payment.approvedAmount = BigDecimal.ZERO;
        payment.canceledAmount = BigDecimal.ZERO;
        payment.currency = "KRW";
        payment.orderSnapshot = orderSnapshot;
        payment.preparedAt = LocalDateTime.now();
        payment.updatedAt = payment.preparedAt;
        return payment;
    }

    public void assignWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
        touch();
    }

    public void assignOrder(Order order) {
        this.order = order;
        touch();
    }

    public void markDone(
            String paymentKey,
            String method,
            String tossStatus,
            BigDecimal approvedAmount,
            String rawResponse
    ) {
        this.status = PaymentStatus.DONE;
        this.paymentKey = paymentKey;
        this.method = method;
        this.tossStatus = tossStatus;
        this.approvedAmount = approvedAmount == null ? this.amount : approvedAmount;
        this.rawResponse = rawResponse;
        this.failCode = null;
        this.failMessage = null;
        this.approvedAt = LocalDateTime.now();
        touch();
    }

    public void markCanceled(BigDecimal cancelAmount, String tossStatus, String rawResponse) {
        if (cancelAmount == null || cancelAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("cancelAmount는 0보다 커야 합니다.");
        }
        this.canceledAmount = this.canceledAmount.add(cancelAmount);
        this.tossStatus = tossStatus;
        this.rawResponse = rawResponse;
        this.canceledAt = LocalDateTime.now();

        if (this.canceledAmount.compareTo(this.approvedAmount) >= 0) {
            this.status = PaymentStatus.CANCELED;
        } else {
            this.status = PaymentStatus.PARTIAL_CANCELED;
        }
        touch();
    }

    public void markFailed(String failCode, String failMessage, String rawResponse) {
        this.status = PaymentStatus.FAILED;
        this.failCode = failCode;
        this.failMessage = failMessage;
        this.rawResponse = rawResponse;
        touch();
    }

    public BigDecimal getRemainingCancelableAmount() {
        return approvedAmount.subtract(canceledAmount);
    }

    public boolean isFullyCanceled() {
        return canceledAmount.compareTo(approvedAmount) >= 0 && approvedAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    @PrePersist
    @PreUpdate
    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
