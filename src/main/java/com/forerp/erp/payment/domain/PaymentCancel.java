package com.forerp.erp.payment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payment_cancels",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_cancel_cancel_key", columnNames = "cancel_key")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentCancel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_cancel_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "cancel_key", nullable = false, length = 100)
    private String cancelKey;

    @Column(name = "reason", nullable = false, length = 300)
    private String reason;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "discard_stock", nullable = false)
    private boolean discardStock;

    @Column(name = "stock_refunded", nullable = false)
    private boolean stockRefunded;

    @Column(name = "order_status_before", length = 30)
    private String orderStatusBefore;

    @Lob
    @Column(name = "raw_response", columnDefinition = "LONGTEXT")
    private String rawResponse;

    @Column(name = "canceled_at", nullable = false)
    private LocalDateTime canceledAt;

    public static PaymentCancel create(
            Payment payment,
            String cancelKey,
            String reason,
            BigDecimal amount,
            boolean discardStock,
            boolean stockRefunded,
            String orderStatusBefore,
            String rawResponse
    ) {
        PaymentCancel history = new PaymentCancel();
        history.payment = payment;
        history.cancelKey = cancelKey;
        history.reason = reason;
        history.amount = amount;
        history.discardStock = discardStock;
        history.stockRefunded = stockRefunded;
        history.orderStatusBefore = orderStatusBefore;
        history.rawResponse = rawResponse;
        history.canceledAt = LocalDateTime.now();
        return history;
    }
}
