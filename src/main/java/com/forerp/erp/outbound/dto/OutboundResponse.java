package com.forerp.erp.outbound.dto;

import com.forerp.erp.outbound.domain.Outbound;
import com.forerp.erp.outbound.domain.OutboundItem;
import com.forerp.erp.shipment.dto.ShipmentResponse;
import com.forerp.erp.warehouse.domain.Warehouse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutboundResponse {
    private Long outboundId;
    private Long orderId;
    private Long storeId;
    private String storeName;
    private String storeCode;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private String status; // CREATED/CONFIRMED/CANCELED
    private LocalDateTime createdAt;
    private ShipmentResponse shipment;
    private List<OutboundItemResponse> items;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class OutboundItemResponse {
        private Long outboundItemId;
        private Long productId;
        private int qty;
        private BigDecimal unitPrice;

        public static OutboundItemResponse from(OutboundItem item) {
            return new OutboundItemResponse(
                    item.getId(),
                    item.getOrderItem().getProduct().getId(),
                    item.getQuantity(),
                    item.getOrderItem().getUnitPrice()
            );
        }
    }

    public static OutboundResponse from(Outbound outbound) {
        Warehouse warehouse = extractWarehouse(outbound);
        return new OutboundResponse(
                outbound.getId(),
                outbound.getOrder().getId(),
                outbound.getStore().getId(),
                outbound.getStore().getName(),
                outbound.getStore().getStoreCode(),
                warehouse == null ? null : warehouse.getId(),
                warehouse == null ? null : warehouse.getCode(),
                warehouse == null ? null : warehouse.getName(),
                outbound.getStatus().name(),
                outbound.getCreatedAt(),
                outbound.getShipment() == null ? null : ShipmentResponse.from(outbound.getShipment()),
                outbound.getItems().stream().map(OutboundResponse.OutboundItemResponse::from).toList()
        );
    }

    private static Warehouse extractWarehouse(Outbound outbound) {
        if (outbound.getItems() == null || outbound.getItems().isEmpty()) {
            return null;
        }
        if (outbound.getItems().get(0).getStoreProduct() == null) {
            return null;
        }
        return outbound.getItems().get(0).getStoreProduct().getWarehouse();
    }
}
