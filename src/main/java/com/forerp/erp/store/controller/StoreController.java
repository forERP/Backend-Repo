package com.forerp.erp.store.controller;

import com.forerp.erp.store.dto.StoreDto;
import com.forerp.erp.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // 매장 생성
    @PostMapping
    public ResponseEntity<StoreDto.Response> createStore(@RequestBody @Valid StoreDto.CreateRequest request) {
        return ResponseEntity.ok(storeService.createStore(request));
    }

    // 전체 매장 조회
    @GetMapping
    public ResponseEntity<List<StoreDto.Response>> getAllStores() {
        return ResponseEntity.ok(storeService.getAllStores());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<StoreDto.Response>> searchStores(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String status,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(storeService.searchStores(name, code, status, pageable));
    }

    // 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<StoreDto.Response> getStore(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.getStore(id));
    }

    // 상태 변경
    @PatchMapping("/{id}/status")
    public ResponseEntity<StoreDto.Response> updateStatus(
            @PathVariable Long id,
            @RequestBody StoreDto.UpdateStatusRequest request) {
        return ResponseEntity.ok(storeService.updateStoreStatus(id, request));
    }

    // 매장 정보 수정
    @PutMapping("/{id}")
    public ResponseEntity<StoreDto.Response> updateStore(
            @PathVariable Long id,
            @RequestBody @Valid StoreDto.UpdateRequest request) {
        return ResponseEntity.ok(storeService.updateStore(id, request));
    }
}
