package com.forerp.erp.product.service;

import com.forerp.erp.product.domain.*;
import com.forerp.erp.product.dto.*;
import com.forerp.erp.product.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    @Transactional
    public ProductCreateResponseDto createProduct(ProductCreateRequestDto request) {

        ProductCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리"));

        // 1. SKU 없이 먼저 저장 (ID 확보)
        Product prd = Product.builder()
                .name(request.getName())
                .category(category)
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .msrpPrice(request.getPrice())
                .status(ProductStatus.ACTIVE)
                .build();

        productRepository.save(prd);

        // 2. SKU 생성
        String sku = generateSku(prd.getId());

        // 3. SKU 반영
        prd.updateSku(sku);

        return new ProductCreateResponseDto(prd.getId(), sku);
    }

    // sku 형태: PRD2026000123
    private String generateSku(Long productId) {
        int year = Year.now().getValue();
        return String.format("PRD%d%06d", year, productId);
    }
}