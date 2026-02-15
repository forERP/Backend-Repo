package com.forerp.erp.product.service;

import com.forerp.erp.common.query.QueryParamParser;
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

        // 1. 임시 SKU로 먼저 저장 (ID 확보)
        Product prd = Product.builder()
                .name(request.getName())
                .category(category)
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .msrpPrice(request.getPrice())
                .status(ProductStatus.ACTIVE)
                .sku("TEMP")  // 임시값
                .build();

        productRepository.save(prd);

        // 2. 아이디 값을 반영하여 실제 SKU 생성 및 저장
        String sku = String.format("PRD%d%06d", Year.now().getValue(), prd.getId());
        prd.updateSku(sku);
        productRepository.save(prd);

        return new ProductCreateResponseDto(prd.getId(), sku);
    }

    // 관리자용 상품 목록 단순 조회
    @Transactional(readOnly = true)
    public Page<ProductListResponseDto> getAllProducts(String name, String sku, String status, Pageable pageable){
        ProductStatus productStatus = QueryParamParser.parseEnumOrNull(status, ProductStatus.class, "status");

        return productRepository.search(normalize(name), normalize(sku), productStatus, pageable)
                .map(page -> new ProductListResponseDto(
                        page.getId(),
                        page.getSku(),
                        page.getName(),
                        page.getCategory().getName(),
                        page.getMsrpPrice(),
                        page.getStatus()
                ));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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

    // 상품 재등록 (단종 취소)
    @Transactional
    public void reactivateProduct(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        if(product.getStatus() != ProductStatus.DISCONTINUED){
            throw new IllegalArgumentException("단종 처리된 상품만 재등록할 수 있습니다.");
        }
        product.reactivate();
    }
}
