package com.forerp.erp.product.service;

import com.forerp.erp.product.domain.ProductCategory;
import com.forerp.erp.product.dto.ProductCategoryCreateRequestDto;
import com.forerp.erp.product.dto.ProductCategoryCreateResponseDto;
import com.forerp.erp.product.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;

    @Transactional
    public ProductCategoryCreateResponseDto createCategory(
            ProductCategoryCreateRequestDto request
    ) {
        // 1. code 중복 검증
        categoryRepository.findByCode(request.getCode())
                .ifPresent(c -> {
                    throw new IllegalArgumentException("이미 존재하는 카테고리 코드");
                });

        // 2. 엔티티 생성
        ProductCategory category = ProductCategory.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .build();

        // 3. 저장
        categoryRepository.save(category);

        return new ProductCategoryCreateResponseDto(
                category.getId(),
                category.getCode()
        );
    }
}
