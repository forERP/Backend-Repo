package com.forerp.erp.returns.domain;

import com.forerp.erp.order.domain.Order;
import com.forerp.erp.payment.domain.Payment;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.warehouse.domain.Warehouse;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "sales_returns",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_sales_return_order", columnNames = "order_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sales_return_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;

    @Column(name = "reason", nullable = false, length = 300)
    private String reason;

    @Column(name = "discard_stock", nullable = false)
    private boolean discardStock;

    @Column(name = "refunded_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal refundedAmount;

    @Column(name = "order_status_before", nullable = false, length = 30)
    private String orderStatusBefore;

    @Column(name = "order_status_after", nullable = false, length = 30)
    private String orderStatusAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReturnStatus status;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    public static SalesReturn processed(
            Order order,
            Payment payment,
            Store store,
            Warehouse warehouse,
            User processedBy,
            String reason,
            boolean discardStock,
            BigDecimal refundedAmount,
            String orderStatusBefore,
            String orderStatusAfter
    ) {
        SalesReturn salesReturn = new SalesReturn();
        salesReturn.order = order;
        salesReturn.payment = payment;
        salesReturn.store = store;
        salesReturn.warehouse = warehouse;
        salesReturn.processedBy = processedBy;
        salesReturn.reason = reason;
        salesReturn.discardStock = discardStock;
        salesReturn.refundedAmount = refundedAmount;
        salesReturn.orderStatusBefore = orderStatusBefore;
        salesReturn.orderStatusAfter = orderStatusAfter;
        salesReturn.status = ReturnStatus.PROCESSED;
        salesReturn.processedAt = LocalDateTime.now();
        return salesReturn;
    }
}

