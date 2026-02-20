package com.forerp.erp.purchase_order.dto;

import com.forerp.erp.purchase_order.domain.PurchaseOrder;
import com.forerp.erp.purchase_order.domain.PurchaseOrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponse {

    private Long purchaseOrderId;
    private String documentNumber;
    private Long purchaseRequestId;
    private LocalDateTime purchaseRequestCreatedAt;
    private String purchaseRequestStatus;
    private Long supplierId;
    private Long storeId;
    private Long warehouseId;
    private Long authoredByUserId;
    private String authoredByName;
    private String authoredByCode;
    private LocalDate deliveryDueDate;
    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;
    private String paymentTerms;
    private String memo;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime orderedAt;
    private List<Item> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long purchaseOrderItemId;
        private Long productId;
        private int qty;

        public static Item from(PurchaseOrderItem it) {
            return new Item(it.getId(), it.getProduct().getId(), it.getQuantity());
        }
    }

    public static PurchaseOrderResponse from(PurchaseOrder po) {
        return new PurchaseOrderResponse(
                po.getId(),
                toDocumentNumber(po),
                po.getPurchaseRequest() == null ? null : po.getPurchaseRequest().getId(),
                po.getPurchaseRequest() == null ? null : po.getPurchaseRequest().getCreatedAt(),
                po.getPurchaseRequest() == null ? null : po.getPurchaseRequest().getStatus().name(),
                po.getSupplier().getId(),
                po.getStore().getId(),
                po.getWarehouse().getId(),
                po.getAuthoredBy() == null ? null : po.getAuthoredBy().getId(),
                po.getAuthoredBy() == null ? null : po.getAuthoredBy().getName(),
                po.getAuthoredBy() == null ? null : po.getAuthoredBy().getEmployeeCode(),
                po.getDeliveryDueDate(),
                po.getReceiverName(),
                po.getReceiverPhone(),
                po.getShippingAddress(),
                po.getPaymentTerms(),
                po.getMemo(),
                po.getStatus().name(),
                po.getCreatedAt(),
                po.getOrderedAt(),
                po.getItems().stream().map(Item::from).toList()
        );
    }

    private static String toDocumentNumber(PurchaseOrder po) {
        if (po.getId() == null || po.getCreatedAt() == null) {
            return po.getId() == null ? null : String.valueOf(po.getId());
        }
        return po.getCreatedAt().format(DateTimeFormatter.ofPattern("yyMMdd")) + "-" + String.format("%04d", po.getId());
    }
}
