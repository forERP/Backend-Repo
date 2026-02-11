package com.forerp.erp.product.service;

import com.forerp.erp.product.domain.ProductCategory;
import com.forerp.erp.product.dto.ProductCategoryCreateRequestDto;
import com.forerp.erp.product.dto.ProductCategoryCreateResponseDto;
import com.forerp.erp.product.dto.ProductCategoryResponseDto;
import com.forerp.erp.product.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;

    public List<ProductCategoryResponseDto> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(c -> new ProductCategoryResponseDto(c.getId(), c.getCode(), c.getName(), c.getDescription()))
                .collect(Collectors.toList());
    }

    public ProductCategoryResponseDto getCategory(Long id) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리"));
        return new ProductCategoryResponseDto(category.getId(), category.getCode(), category.getName(), category.getDescription());
    }

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
