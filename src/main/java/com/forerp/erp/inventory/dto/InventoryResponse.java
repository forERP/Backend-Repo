package com.forerp.erp.inventory.dto;

import com.forerp.erp.storeproduct.domain.SaleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "재고 상세 응답")
public class InventoryResponse {

    @Schema(description = "매장 상품 ID", example = "1")
    private Long storeProductId;

    @Schema(description = "매장 ID", example = "1")
    private Long storeId;

    @Schema(description = "매장 이름", example = "강남점")
    private String storeName;

    @Schema(description = "매장 코드", example = "STORE001")
    private String storeCode;

    @Schema(description = "창고 ID", example = "1")
    private Long warehouseId;

    @Schema(description = "창고 코드", example = "WH001")
    private String warehouseCode;

    @Schema(description = "창고 이름", example = "중앙 창고")
    private String warehouseName;

    @Schema(description = "상품 ID", example = "1")
    private Long productId;

    @Schema(description = "SKU", example = "SKU-001")
    private String sku;

    @Schema(description = "상품 이름", example = "아메리카노")
    private String productName;

    @Schema(description = "현재 재고 수량", example = "100")
    private int onHand;

    @Schema(description = "판매 상태")
    private SaleStatus saleStatus;

    @Schema(description = "매장 판매가", example = "4500")
    private BigDecimal salePrice;

    @Schema(description = "마지막 수정 일시")
    private LocalDateTime updatedAt;
}