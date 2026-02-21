package com.forerp.erp.product.service;

import com.forerp.erp.auditlog.AuditLogAction;
import com.forerp.erp.auditlog.AuditLogService;
import com.forerp.erp.auditlog.AuditLogTargetType;
import com.forerp.erp.product.domain.Product;
import com.forerp.erp.product.domain.ProductCategory;
import com.forerp.erp.product.dto.ProductCategoryCreateRequestDto;
import com.forerp.erp.product.dto.ProductCategoryCreateResponseDto;
import com.forerp.erp.product.dto.ProductCategoryDetailResponseDto;
import com.forerp.erp.product.dto.ProductCategoryProductResponseDto;
import com.forerp.erp.product.dto.ProductCategoryResponseDto;
import com.forerp.erp.product.dto.ProductCategoryUpdateRequestDto;
import com.forerp.erp.product.repository.ProductCategoryRepository;
import com.forerp.erp.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<ProductCategoryResponseDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toSimpleResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ProductCategoryResponseDto> searchCategories(String keyword, String name, String code, String status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return categoryRepository.search(
                        normalize(keyword),
                        normalize(name),
                        normalize(code),
                        parseActiveStatus(status),
                        pageable
                )
                .map(this::toSimpleResponse);
    }

    @Transactional(readOnly = true)
    public ProductCategoryDetailResponseDto getCategory(Long id) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리"));

        List<ProductCategoryProductResponseDto> products = productRepository.findByCategory_IdOrderByNameAsc(id).stream()
                .map(this::toCategoryProductResponse)
                .toList();

        return new ProductCategoryDetailResponseDto(
                category.getId(),
                category.getCode(),
                category.getName(),
                category.getDescription(),
                category.getImageUrl(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt(),
                products
        );
    }

    @Transactional
    public ProductCategoryCreateResponseDto createCategory(ProductCategoryCreateRequestDto request) {
        String code = request.getCode().trim();
        String name = request.getName().trim();

        categoryRepository.findByCode(code)
                .ifPresent(c -> {
                    throw new IllegalArgumentException("이미 존재하는 카테고리 코드입니다.");
                });

        ProductCategory category = ProductCategory.builder()
                .code(code)
                .name(name)
                .description(trimToNull(request.getDescription()))
                .imageUrl(trimToNull(request.getImageUrl()))
                .active(request.getActive() == null || request.getActive())
                .build();

        categoryRepository.save(category);
        auditLogService.logCurrentUserAction(
                AuditLogAction.PRODUCT_CATEGORY_CREATE,
                AuditLogTargetType.PRODUCT_CATEGORY,
                category.getId()
        );

        return new ProductCategoryCreateResponseDto(category.getId(), category.getCode());
    }

    @Transactional
    public ProductCategoryDetailResponseDto updateCategory(Long id, ProductCategoryUpdateRequestDto request) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리"));

        String code = request.getCode().trim();
        String name = request.getName().trim();

        if (categoryRepository.existsByCodeAndIdNot(code, id)) {
            throw new IllegalArgumentException("이미 존재하는 카테고리 코드입니다.");
        }

        category.update(
                code,
                name,
                trimToNull(request.getDescription()),
                trimToNull(request.getImageUrl()),
                request.getActive() == null ? category.isActive() : request.getActive()
        );
        auditLogService.logCurrentUserAction(
                AuditLogAction.PRODUCT_CATEGORY_UPDATE,
                AuditLogTargetType.PRODUCT_CATEGORY,
                category.getId()
        );

        return getCategory(id);
    }

    private ProductCategoryResponseDto toSimpleResponse(ProductCategory category) {
        return new ProductCategoryResponseDto(
                category.getId(),
                category.getCode(),
                category.getName(),
                category.getDescription(),
                category.getImageUrl(),
                category.isActive()
        );
    }

    private ProductCategoryProductResponseDto toCategoryProductResponse(Product product) {
        return new ProductCategoryProductResponseDto(
                product.getId(),
                product.getSku(),
                product.getName()
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimToNull(String value) {
        return normalize(value);
    }

    private Boolean parseActiveStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        return switch (status.trim().toUpperCase()) {
            case "ACTIVE" -> true;
            case "INACTIVE" -> false;
            default -> throw new IllegalArgumentException("Invalid category status: " + status);
        };
    }
}
