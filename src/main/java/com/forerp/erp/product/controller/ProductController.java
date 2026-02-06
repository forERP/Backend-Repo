package com.forerp.erp.product.controller;

import com.forerp.erp.product.dto.*;
import com.forerp.erp.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 상품 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductCreateResponseDto createProduct(
            @RequestBody @Valid ProductCreateRequestDto request
    ) {
        return productService.createProduct(request);
    }

    // 상품 목록 조회(관리자용)
    @GetMapping
    public ResponseEntity<Page<ProductListResponseDto>> getProducts(
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ){
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    // 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto.DetailResponse> getProduct(@PathVariable Long id){
        return ResponseEntity.ok(productService.getProduct(id));
    }

    // 상품 수정
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto.DetailResponse> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductDto.UpdateRequest request){
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    // 상품 삭제
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    )
}