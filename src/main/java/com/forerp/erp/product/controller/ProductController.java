package com.forerp.erp.product.controller;

import com.forerp.erp.product.dto.*;
import com.forerp.erp.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductCreateResponseDto createProduct(
            @RequestBody @Valid ProductCreateRequestDto request
    ) {
        return productService.createProduct(request);
    }
}