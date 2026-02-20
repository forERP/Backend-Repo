package com.forerp.erp.purchase_req.service;

import com.forerp.erp.common.query.QueryParamParser;
import com.forerp.erp.purchase_order.domain.PurchaseOrder;
import com.forerp.erp.purchase_order.domain.PurchaseOrderStatus;
import com.forerp.erp.purchase_order.repository.PurchaseOrderRepository;
import com.forerp.erp.purchase_order.service.support.PurchaseOrderBuilder;
import com.forerp.erp.purchase_req.domain.PurchaseRequest;
import com.forerp.erp.purchase_req.domain.PurchaseRequestStatus;
import com.forerp.erp.purchase_req.dto.PurchaseRequestApproveRequest;
import com.forerp.erp.purchase_req.dto.PurchaseRequestCreateRequest;
import com.forerp.erp.purchase_req.dto.PurchaseRequestListResponse;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseRequestService {

    private final PurchaseRequestRepository purchaseRequestRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    private final PurchaseRequestLoader loader;
    private final PurchaseRequestBuilder prBuilder;
    private final PurchaseOrderBuilder poBuilder;

    public PurchaseRequest create(PurchaseRequestCreateRequest req, User actor) {
        PurchaseRequest pr = prBuilder.buildCreateAggregate(req, actor);
        return purchaseRequestRepository.save(pr);
    }

    @Transactional(readOnly = true)
    public PurchaseRequest get(Long id) {
        return loader.loadPurchaseRequest(id);
    }

    @Transactional(readOnly = true)
    public PurchaseRequestListResponse list(
            Long storeId,
            String storeName,
            String storeCode,
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
        Page<PurchaseRequest> result = purchaseRequestRepository.search(
                storeId,
                normalize(storeName),
                normalize(storeCode),
                st,
                fromDt,
                toDt,
                pageable
        );

        List<PurchaseRequestListResponse.Item> content = result.getContent().stream()
                .map(pr -> new PurchaseRequestListResponse.Item(
                        pr.getId(),
                        pr.getStore().getId(),
                        pr.getStore().getName(),
                        pr.getStore().getStoreCode(),
                        pr.getRequestedBy() == null ? null : pr.getRequestedBy().getId(),
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

    @Transactional(readOnly = true)
    public Optional<PurchaseOrder> getDraftOrder(Long purchaseRequestId) {
        return purchaseOrderRepository.findByPurchaseRequest_Id(purchaseRequestId);
    }

    public PurchaseOrder createDraftOrder(Long purchaseRequestId, PurchaseRequestApproveRequest req, User actor) {
        PurchaseRequest pr = loader.loadPurchaseRequest(purchaseRequestId);

        if (pr.getStatus() != PurchaseRequestStatus.REQUESTED) {
            throw new IllegalStateException("요청 상태가 REQUESTED가 아닙니다.");
        }

        if (purchaseOrderRepository.findByPurchaseRequest_Id(purchaseRequestId).isPresent()) {
            throw new IllegalStateException("이미 발주서가 작성되어 있습니다.");
        }

        Supplier supplier = loader.loadSupplier(req.getSupplierId());
        Warehouse warehouse = loader.loadWarehouse(req.getWarehouseId());
        Store store = pr.getStore();
        String receiverName = normalizeReceiverName(req.getReceiverName(), pr);
        String receiverPhone = normalizeOrDefault(req.getReceiverPhone(), pr.getRequestedBy() == null ? null : pr.getRequestedBy().getPhoneNumber());
        String shippingAddress = normalizeOrDefault(req.getShippingAddress(), warehouse.getAddress());
        String paymentTerms = normalizeOrDefault(req.getPaymentTerms(), "월말정산");
        String memo = normalizeOrDefault(req.getMemo(), pr.getMemo());

        PurchaseOrder po = poBuilder.buildFromApprovedRequest(
                pr,
                supplier,
                store,
                warehouse,
                actor,
                req.getDeliveryDueDate(),
                receiverName,
                receiverPhone,
                shippingAddress,
                paymentTerms,
                memo
        );

        return purchaseOrderRepository.save(po);
    }

    public PurchaseOrder approve(Long purchaseRequestId) {
        PurchaseRequest pr = loader.loadPurchaseRequest(purchaseRequestId);

        if (pr.getStatus() != PurchaseRequestStatus.REQUESTED) {
            throw new IllegalStateException("요청 상태가 REQUESTED가 아닙니다.");
        }

        PurchaseOrder po = purchaseOrderRepository.findByPurchaseRequest_Id(purchaseRequestId)
                .orElseThrow(() -> new IllegalStateException("발주서를 먼저 작성해주세요."));

        if (po.getStatus() != PurchaseOrderStatus.CREATED) {
            throw new IllegalStateException("작성 단계의 발주서가 아닙니다.");
        }

        pr.approve();
        return po;
    }

    public PurchaseRequest reject(Long purchaseRequestId) {
        PurchaseRequest pr = loader.loadPurchaseRequest(purchaseRequestId);

        purchaseOrderRepository.findByPurchaseRequest_Id(purchaseRequestId)
                .ifPresent(po -> {
                    if (po.getStatus() != PurchaseOrderStatus.CREATED) {
                        throw new IllegalStateException("이미 처리 중인 발주가 있어 반려할 수 없습니다.");
                    }
                    purchaseOrderRepository.delete(po);
                });

        pr.reject();
        return pr;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeOrDefault(String value, String defaultValue) {
        String normalized = normalize(value);
        return normalized == null ? defaultValue : normalized;
    }

    private String normalizeReceiverName(String receiverName, PurchaseRequest request) {
        String normalized = normalize(receiverName);
        if (normalized != null) {
            return normalized;
        }

        String requesterName = request.getRequestedBy() == null ? null : normalize(request.getRequestedBy().getName());
        if (requesterName != null) {
            return requesterName;
        }
        return request.getStore() == null ? null : normalize(request.getStore().getName());
    }
}
