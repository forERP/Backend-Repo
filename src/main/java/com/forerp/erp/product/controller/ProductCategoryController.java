package com.forerp.erp.product.controller;

import com.forerp.erp.product.dto.ProductCategoryCreateRequestDto;
import com.forerp.erp.product.dto.ProductCategoryCreateResponseDto;
import com.forerp.erp.product.service.ProductCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product-categories")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductCategoryCreateResponseDto createCategory(
            @RequestBody @Valid ProductCategoryCreateRequestDto request
    ) {
        return categoryService.createCategory(request);
    }
}