package com.forerp.erp.dashboard.service;

import com.forerp.erp.dashboard.dto.DashboardAlertsResponse;
import com.forerp.erp.dashboard.dto.DashboardRecentOrderItemResponse;
import com.forerp.erp.dashboard.dto.DashboardSummaryResponse;
import com.forerp.erp.inbound.domain.InboundStatus;
import com.forerp.erp.inbound.repository.InboundRepository;
import com.forerp.erp.order.domain.Order;
import com.forerp.erp.order.domain.OrderStatus;
import com.forerp.erp.order.repository.OrderRepository;
import com.forerp.erp.outbound.domain.OutboundStatus;
import com.forerp.erp.outbound.repository.OutboundRepository;
import com.forerp.erp.purchase_req.domain.PurchaseRequestStatus;
import com.forerp.erp.purchase_req.repository.PurchaseRequestRepository;
import com.forerp.erp.storeproduct.repository.StoreProductRepository;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.domain.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final int DEFAULT_RECENT_ORDER_LIMIT = 10;
    private static final int MAX_RECENT_ORDER_LIMIT = 20;
    private static final long DELAYED_ORDER_MINUTES = 30L;

    private final OrderRepository orderRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final OutboundRepository outboundRepository;
    private final InboundRepository inboundRepository;
    private final StoreProductRepository storeProductRepository;

    public DashboardSummaryResponse getSummary(User actor) {
        DashboardMetrics metrics = loadMetrics(actor);

        return new DashboardSummaryResponse(
                metrics.todayOrderCount(),
                metrics.todaySalesAmount(),
                metrics.todayOutboundCount(),
                metrics.todayInboundCount(),
                metrics.pendingPurchaseApprovalCount(),
                metrics.lowStockSkuCount(),
                metrics.delayedOrderCount(),
                metrics.pendingOutboundConfirmCount(),
                metrics.pendingInboundConfirmCount()
        );
    }

    public DashboardAlertsResponse getAlerts(User actor) {
        DashboardMetrics metrics = loadMetrics(actor);

        return new DashboardAlertsResponse(
                metrics.pendingPurchaseApprovalCount(),
                metrics.lowStockSkuCount(),
                metrics.delayedOrderCount(),
                metrics.pendingOutboundConfirmCount(),
                metrics.pendingInboundConfirmCount()
        );
    }

    public List<DashboardRecentOrderItemResponse> getRecentOrders(User actor, Integer limit) {
        Long storeScopeId = resolveStoreScope(actor);
        int resolvedLimit = resolveRecentOrderLimit(limit);

        return orderRepository.findRecentForDashboard(storeScopeId, PageRequest.of(0, resolvedLimit))
                .getContent()
                .stream()
                .map(this::toRecentOrderItem)
                .toList();
    }

    private DashboardRecentOrderItemResponse toRecentOrderItem(Order order) {
        return new DashboardRecentOrderItemResponse(
                order.getId(),
                order.getStore() == null ? null : order.getStore().getName(),
                order.getStatus() == null ? null : order.getStatus().name(),
                order.getTotalAmount(),
                order.getOrderedAt()
        );
    }

    private DashboardMetrics loadMetrics(User actor) {
        Long storeScopeId = resolveStoreScope(actor);
        boolean isHqAdmin = actor.getRole() == UserRole.HQ_ADMIN;

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = startOfToday.plusDays(1);
        LocalDateTime delayedThreshold = LocalDateTime.now().minusMinutes(DELAYED_ORDER_MINUTES);

        long todayOrderCount = orderRepository.countForDashboard(storeScopeId, startOfToday, startOfTomorrow);
        BigDecimal todaySalesAmount = orderRepository.sumSalesAmountForDashboard(storeScopeId, startOfToday, startOfTomorrow);
        long todayOutboundCount = outboundRepository.countForDashboard(
                storeScopeId,
                List.of(OutboundStatus.CONFIRMED, OutboundStatus.ARRIVED),
                startOfToday,
                startOfTomorrow
        );
        long todayInboundCount = inboundRepository.countForDashboard(
                storeScopeId,
                InboundStatus.CONFIRMED,
                startOfToday,
                startOfTomorrow
        );
        long delayedOrderCount = orderRepository.countDelayedForDashboard(
                storeScopeId,
                OrderStatus.PLACED,
                delayedThreshold
        );
        long pendingOutboundConfirmCount = outboundRepository.countByStatusForDashboard(
                storeScopeId,
                OutboundStatus.CREATED
        );
        long pendingInboundConfirmCount = inboundRepository.countByStatusForDashboard(
                storeScopeId,
                InboundStatus.CREATED
        );

        long pendingPurchaseApprovalCount = isHqAdmin
                ? purchaseRequestRepository.countByStatusForDashboard(null, PurchaseRequestStatus.REQUESTED)
                : 0L;

        long lowStockSkuCount = isHqAdmin
                ? 0L
                : storeProductRepository.countLowStockSkuForDashboard(storeScopeId);

        return new DashboardMetrics(
                todayOrderCount,
                todaySalesAmount == null ? BigDecimal.ZERO : todaySalesAmount,
                todayOutboundCount,
                todayInboundCount,
                pendingPurchaseApprovalCount,
                lowStockSkuCount,
                delayedOrderCount,
                pendingOutboundConfirmCount,
                pendingInboundConfirmCount
        );
    }

    private Long resolveStoreScope(User actor) {
        if (actor == null || actor.getRole() == null) {
            throw new AccessDeniedException("Authentication is required.");
        }

        if (actor.getRole() == UserRole.HQ_ADMIN) {
            return null;
        }

        if (actor.getRole() == UserRole.STORE_ADMIN) {
            Long storeId = actor.getStore() == null ? null : actor.getStore().getId();
            if (storeId == null) {
                throw new AccessDeniedException("Store admin account must be assigned to a store.");
            }
            return storeId;
        }

        throw new AccessDeniedException("Only HQ/STORE admins can access dashboard.");
    }

    private int resolveRecentOrderLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_RECENT_ORDER_LIMIT;
        }
        return Math.min(limit, MAX_RECENT_ORDER_LIMIT);
    }

    private record DashboardMetrics(
            long todayOrderCount,
            BigDecimal todaySalesAmount,
            long todayOutboundCount,
            long todayInboundCount,
            long pendingPurchaseApprovalCount,
            long lowStockSkuCount,
            long delayedOrderCount,
            long pendingOutboundConfirmCount,
            long pendingInboundConfirmCount
    ) {
    }
}
