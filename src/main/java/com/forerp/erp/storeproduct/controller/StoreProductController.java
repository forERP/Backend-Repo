package com.forerp.erp.storeproduct.controller;

import com.forerp.erp.store.dto.StoreProductListResponseDto;
import com.forerp.erp.storeproduct.service.StoreProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store_products")
@RequiredArgsConstructor
public class StoreProductController {

    private final StoreProductService storeProductService;

    @GetMapping("/list")
    public ResponseEntity<Page<StoreProductListResponseDto>> getStoreProductList(
            @RequestParam Long storeId,
            @PageableDefault(size = 20) Pageable pageable){

        return ResponseEntity.ok(storeProductService.getStoreProductList(storeId,pageable));
    }
}
