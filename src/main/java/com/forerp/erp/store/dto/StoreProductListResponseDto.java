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
    private final String categoryImageUrl;
    private final String imageUrl;
    private final BigDecimal msrpPrice;

    // 본인 매장 정보
    private final boolean isRegistered;
    private final Integer quantity;
    private final SaleStatus saleStatus;
    private final BigDecimal salePrice;

    public StoreProductListResponseDto(
            Long productId,
            String sku,
            String name,
            String categoryName,
            String categoryImageUrl,
            String imageUrl,
            BigDecimal msrpPrice,
            Long storeProductId,
            Number quantity,
            SaleStatus saleStatus,
            BigDecimal salePrice
    ){
      this.productId = productId;
      this.sku = sku;
      this.name = name;
      this.categoryName = categoryName;
      this.categoryImageUrl = categoryImageUrl;
      this.imageUrl = imageUrl;
      this.msrpPrice = msrpPrice;

      this.isRegistered = (storeProductId != null);
      this.quantity = (quantity != null) ? quantity.intValue() : 0;
      this.saleStatus = (saleStatus != null) ? saleStatus : SaleStatus.OFF;
      this.salePrice = (salePrice != null) ? salePrice : BigDecimal.ZERO;
    }
}
