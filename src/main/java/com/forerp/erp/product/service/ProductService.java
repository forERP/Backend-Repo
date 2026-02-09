package com.forerp.erp.product.service;

import com.forerp.erp.product.domain.*;
import com.forerp.erp.product.dto.*;
import com.forerp.erp.product.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // 관리자용 상품 목록 단순 조회
    @Transactional(readOnly = true)
    public Page<ProductListResponseDto> getAllProducts(Pageable pageable){
        return productRepository.findAll(pageable)
                .map(page -> new ProductListResponseDto(
                        page.getId(),
                        page.getSku(),
                        page.getName(),
                        page.getCategory().getName(),
                        page.getMsrpPrice(),
                        page.getStatus()
                ));
    }

    // 단건 조회
    @Transactional(readOnly = true)
    public ProductDto.DetailResponse getProduct(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        return new ProductDto.DetailResponse(product);
    }

    // 상품 수정
    @Transactional
    public ProductDto.DetailResponse updateProduct(Long id, ProductDto.UpdateRequest request){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // 카테고리 변경 시 조회
        ProductCategory category = null;
        if(request.getCategoryId() != null){
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리"));
        }

        product.update(
                request.getName(),
                category,
                request.getMsrpPrice(),
                request.getDescription(),
                request.getImageUrl()
        );

        return new ProductDto.DetailResponse(product);
    }

    // 상품 단종 처리
    @Transactional
    public void discontinueProduct(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        product.discontinue();
    }

    // sku 형태: PRD2026000123
    private String generateSku(Long productId) {
        int year = Year.now().getValue();
        return String.format("PRD%d%06d", year, productId);
    }
}