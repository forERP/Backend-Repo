package com.forerp.erp.store.dto;

import com.forerp.erp.storeproduct.domain.SaleStatus;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class StoreProductListResponseDto {

    private final Long productId;
    private final String sku;
    private final String name;
    private final String categoryName;
    private final BigDecimal msrpPrice;

    // 본인 매장 정보
    private final boolean isRegistered;
    private final Integer quantity;
    private final SaleStatus saleStatus;
    private final BigDecimal salePrice;

    public StoreProductListResponseDto(
            Long productId, String sku, String name, String categoryName, BigDecimal msrpPrice,
            Long storeProductId, Integer quantity, SaleStatus saleStatus, BigDecimal salePrice
    ){
      this.productId = productId;
      this.sku = sku;
      this.name = name;
      this.categoryName = categoryName;
      this.msrpPrice = msrpPrice;

      this.isRegistered = (storeProductId != null);
      this.quantity = (quantity != null) ? quantity : 0;
      this.saleStatus = (saleStatus != null) ? saleStatus : SaleStatus.OFF;
      this.salePrice = (salePrice != null) ? salePrice : BigDecimal.ZERO;
    }
}
