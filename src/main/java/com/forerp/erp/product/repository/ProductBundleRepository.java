package com.forerp.erp.product.repository;

import com.forerp.erp.product.domain.ProductBundle;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductBundleRepository extends JpaRepository<ProductBundle, Long> {

    boolean existsByProduct_Id(Long productId);

    Optional<ProductBundle> findByProduct_Id(Long productId);

    @EntityGraph(attributePaths = {"product", "items", "items.componentProduct", "items.componentProduct.category"})
    List<ProductBundle> findByProduct_IdIn(Collection<Long> productIds);
}
