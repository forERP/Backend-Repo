package com.forerp.erp.purchase_req.service;

import com.forerp.erp.common.query.QueryParamParser;
import com.forerp.erp.purchase_order.domain.PurchaseOrder;
import com.forerp.erp.purchase_order.dto.PurchaseOrderResponse;
import com.forerp.erp.purchase_order.repository.PurchaseOrderRepository;
import com.forerp.erp.purchase_order.service.support.PurchaseOrderBuilder;
import com.forerp.erp.purchase_req.domain.PurchaseRequest;
import com.forerp.erp.purchase_req.domain.PurchaseRequestStatus;
import com.forerp.erp.purchase_req.dto.PurchaseRequestApproveRequest;
import com.forerp.erp.purchase_req.dto.PurchaseRequestCreateRequest;
import com.forerp.erp.purchase_req.dto.PurchaseRequestListResponse;
import com.forerp.erp.purchase_req.dto.PurchaseRequestResponse;
import com.forerp.erp.purchase_req.repository.PurchaseRequestRepository;
import com.forerp.erp.purchase_req.service.support.PurchaseRequestBuilder;
import com.forerp.erp.purchase_req.service.support.PurchaseRequestLoader;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.supplier.domain.Supplier;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.warehouse.domain.Warehouse;
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
public class PurchaseRequestService {

    private final PurchaseRequestRepository purchaseRequestRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    private final PurchaseRequestLoader loader;
    private final PurchaseRequestBuilder prBuilder;
    private final PurchaseOrderBuilder poBuilder;

    /* 발주요청 생성 */
    public PurchaseRequest create(PurchaseRequestCreateRequest req, User actor) {
        PurchaseRequest pr = prBuilder.buildCreateAggregate(req, actor);
        return purchaseRequestRepository.save(pr);
    }

    /* 발주요청 단건 조회 */
    @Transactional(readOnly = true)
    public PurchaseRequest get(Long id) {
        return loader.loadPurchaseRequest(id);
    }

    /* 발주요청 목록 */
    @Transactional(readOnly = true)
    public PurchaseRequestListResponse list(
            Long storeId,
            String status,
            String from,
            String to,
            int page,
            int size
    ) {
        PurchaseRequestStatus st = QueryParamParser.parseEnumOrNull(status, PurchaseRequestStatus.class, "status");
        LocalDateTime fromDt = QueryParamParser.parseFromDate(from);
        LocalDateTime toDt = QueryParamParser.parseToDateExclusive(to);

        PageRequest pageable = PageRequest.of(page, size);
        Page<PurchaseRequest> result = purchaseRequestRepository.search(storeId, st, fromDt, toDt, pageable);

        List<PurchaseRequestListResponse.Item> content = result.getContent().stream()
                .map(pr -> new PurchaseRequestListResponse.Item(
                        pr.getId(),
                        pr.getStore().getId(),
                        pr.getStatus().name(),
                        pr.getCreatedAt()
                ))
                .toList();

        return new PurchaseRequestListResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    /* 발주요청 승인 = 발주 생성 */
    public PurchaseOrder approve(Long purchaseRequestId, PurchaseRequestApproveRequest req) {
        PurchaseRequest pr = loader.loadPurchaseRequest(purchaseRequestId);

        // 중복 발주 생성 방지
        if (purchaseOrderRepository.existsByPurchaseRequest_Id(purchaseRequestId)) {
            throw new IllegalStateException("이미 발주가 생성된 요청입니다.");
        }

        pr.approve();

        Supplier supplier = loader.loadSupplier(req.getSupplierId());
        Warehouse warehouse = loader.loadWarehouse(req.getWarehouseId());
        Store store = pr.getStore();

        PurchaseOrder po = poBuilder.buildFromApprovedRequest(
                pr,
                supplier,
                store,
                warehouse,
                req.getMemo()
        );

        // 승인된 PR은 같은 트랜잭션에서 자동 dirty-check 반영
        purchaseOrderRepository.save(po);
        return po;
    }

    /* 발주요청 반려 */
    public PurchaseRequest reject(Long purchaseRequestId) {
        PurchaseRequest pr = loader.loadPurchaseRequest(purchaseRequestId);
        pr.reject();
        return pr;
    }
}