package com.forerp.erp.purchase_order.service;

import com.forerp.erp.common.query.QueryParamParser;
import com.forerp.erp.purchase_order.domain.PurchaseOrder;
import com.forerp.erp.purchase_order.domain.PurchaseOrderStatus;
import com.forerp.erp.purchase_order.dto.PurchaseOrderListResponse;
import com.forerp.erp.purchase_order.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;

    /* 발주 단건 조회 */
    @Transactional(readOnly = true)
    public PurchaseOrder get(Long purchaseOrderId) {
        return purchaseOrderRepository.findDetailById(purchaseOrderId)
                .orElseThrow(() -> new IllegalArgumentException("발주를 찾을 수 없습니다."));
    }

    /* 발주 생성 */
    public PurchaseOrder order(Long purchaseOrderId) {
        PurchaseOrder po = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new IllegalArgumentException("발주를 찾을 수 없습니다."));
        po.order();
        return po;
    }

    /* 발주 취소 */
    public PurchaseOrder cancel(Long purchaseOrderId) {
        PurchaseOrder po = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new IllegalArgumentException("발주를 찾을 수 없습니다."));
        po.cancel();
        return po;
    }

    /* 발주 목록 조회 */
    @Transactional(readOnly = true)
    public PurchaseOrderListResponse list(
            Long storeId,
            Long warehouseId,
            String status,
            String from,
            String to,
            int page,
            int size
    ) {
        PurchaseOrderStatus st = QueryParamParser.parseEnumOrNull(status, PurchaseOrderStatus.class, "status");
        LocalDateTime fromDt = QueryParamParser.parseFromDate(from);
        LocalDateTime toDt = QueryParamParser.parseToDateExclusive(to);

        PageRequest pageable = PageRequest.of(page, size);
        Page<PurchaseOrder> result = purchaseOrderRepository.search(storeId, warehouseId, st, fromDt, toDt, pageable);

        List<PurchaseOrderListResponse.Item> content = result.getContent().stream()
                .map(po -> new PurchaseOrderListResponse.Item(
                        po.getId(),
                        po.getPurchaseRequest() == null ? null : po.getPurchaseRequest().getId(),
                        po.getSupplier().getId(),
                        po.getStore().getId(),
                        po.getWarehouse().getId(),
                        po.getStatus().name(),
                        po.getCreatedAt(),
                        po.getOrderedAt()
                ))
                .toList();

        return new PurchaseOrderListResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}