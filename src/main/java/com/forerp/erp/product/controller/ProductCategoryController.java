package com.forerp.erp.product.controller;

import com.forerp.erp.product.dto.ProductCategoryCreateRequestDto;
import com.forerp.erp.product.dto.ProductCategoryCreateResponseDto;
import com.forerp.erp.product.dto.ProductCategoryDetailResponseDto;
import com.forerp.erp.product.dto.ProductCategoryResponseDto;
import com.forerp.erp.product.dto.ProductCategoryUpdateRequestDto;
import com.forerp.erp.product.service.ProductCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-categories")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<ProductCategoryResponseDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductCategoryResponseDto>> searchCategories(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(categoryService.searchCategories(name, code, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductCategoryDetailResponseDto> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategory(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductCategoryCreateResponseDto createCategory(
            @RequestBody @Valid ProductCategoryCreateRequestDto request
    ) {
        return categoryService.createCategory(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductCategoryDetailResponseDto> updateCategory(
            @PathVariable Long id,
            @RequestBody @Valid ProductCategoryUpdateRequestDto request
    ) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }
}
