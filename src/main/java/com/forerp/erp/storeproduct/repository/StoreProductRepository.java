package com.forerp.erp.storeproduct.repository;

import com.forerp.erp.storeproduct.domain.StoreProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreProductRepository extends JpaRepository<StoreProduct, Long> {

}