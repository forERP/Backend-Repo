package com.forerp.erp.inbound.dto;

import com.forerp.erp.inbound.domain.Inbound;
import com.forerp.erp.inbound.domain.InboundItem;
import com.forerp.erp.shipment.dto.ShipmentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InboundResponse {
    private Long inboundId;
    private Long purchaseOrderId;
    private Long storeId;
    private Long warehouseId;
    private String status; // CREATED/CONFIRMED/CANCELED
    private LocalDateTime createdAt;
    private ShipmentResponse shipment;
    private List<InboundItemResponse> items;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class InboundItemResponse {
        private Long inboundItemId;
        private Long productId;
        private int qty;

        public static InboundItemResponse from(InboundItem item) {
            return new InboundItemResponse(
                    item.getId(),
                    item.getProduct().getId(),
                    item.getQuantity()
            );
        }
    }

    public static InboundResponse from(Inbound inbound) {
        return new InboundResponse(
                inbound.getId(),
                inbound.getPurchaseOrder().getId(),
                inbound.getStore().getId(),
                inbound.getWarehouse().getId(),
                inbound.getStatus().name(),
                inbound.getCreatedAt(),
                ShipmentResponse.from(inbound.getShipment()),
                inbound.getItems().stream().map(InboundResponse.InboundItemResponse::from).toList()
        );
    }
}