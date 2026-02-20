package com.forerp.erp.payment.service;

import com.forerp.erp.product.domain.ProductStatus;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.storeproduct.domain.SaleStatus;
import com.forerp.erp.storeproduct.domain.StoreProduct;
import com.forerp.erp.storeproduct.repository.StoreProductRepository;
import com.forerp.erp.warehouse.domain.Warehouse;
import com.forerp.erp.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderStockAllocator {

    private final WarehouseRepository warehouseRepository;
    private final StoreProductRepository storeProductRepository;

    public AllocationResult allocate(Store store, List<RequestedLine> requestLines) {
        if (requestLines == null || requestLines.isEmpty()) {
            throw new IllegalArgumentException("주문 항목이 비어 있습니다.");
        }

        List<RequestedLine> deduplicatedLines = mergeDuplicatedLines(requestLines);
        List<Warehouse> warehouses = warehouseRepository.findByStore_IdAndActiveTrue(store.getId()).stream()
                .sorted(Comparator
                        .comparing(Warehouse::getCode, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(Warehouse::getId))
                .toList();

        if (warehouses.isEmpty()) {
            throw new IllegalStateException("활성 창고가 없습니다.");
        }

        List<Long> warehouseIds = warehouses.stream().map(Warehouse::getId).toList();
        List<Long> productIds = deduplicatedLines.stream().map(RequestedLine::productId).distinct().toList();

        List<StoreProduct> storeProducts = storeProductRepository
                .findByStore_IdAndWarehouse_IdInAndProduct_IdInAndProduct_Status(
                        store.getId(),
                        warehouseIds,
                        productIds,
                        ProductStatus.ACTIVE
                );

        Map<Long, Map<Long, StoreProduct>> byWarehouseProduct = new HashMap<>();
        for (StoreProduct sp : storeProducts) {
            byWarehouseProduct
                    .computeIfAbsent(sp.getWarehouse().getId(), key -> new HashMap<>())
                    .put(sp.getProduct().getId(), sp);
        }

        for (Warehouse warehouse : warehouses) {
            Map<Long, StoreProduct> stockByProduct = byWarehouseProduct.getOrDefault(warehouse.getId(), Map.of());
            List<AllocatedLine> allocatedLines = new ArrayList<>();
            boolean allSatisfied = true;

            for (RequestedLine requestLine : deduplicatedLines) {
                StoreProduct sp = stockByProduct.get(requestLine.productId());

                if (sp == null || sp.getSaleStatus() != SaleStatus.ON || sp.getQuantity() < requestLine.qty()) {
                    allSatisfied = false;
                    break;
                }

                BigDecimal unitPrice = resolveUnitPrice(sp);
                allocatedLines.add(new AllocatedLine(
                        requestLine.productId(),
                        sp.getProduct().getName(),
                        requestLine.qty(),
                        unitPrice
                ));
            }

            if (allSatisfied) {
                BigDecimal totalAmount = allocatedLines.stream()
                        .map(line -> line.unitPrice().multiply(BigDecimal.valueOf(line.qty())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                return new AllocationResult(warehouse, allocatedLines, totalAmount);
            }
        }

        throw new IllegalStateException("선택 가능한 재고가 없습니다. 모든 창고에서 재고가 부족하거나 판매중지 상태입니다.");
    }

    private List<RequestedLine> mergeDuplicatedLines(List<RequestedLine> lines) {
        Map<Long, Integer> merged = new LinkedHashMap<>();
        for (RequestedLine line : lines) {
            if (line.productId() == null || line.qty() <= 0) {
                throw new IllegalArgumentException("상품/수량 정보가 올바르지 않습니다.");
            }
            merged.put(line.productId(), merged.getOrDefault(line.productId(), 0) + line.qty());
        }

        return merged.entrySet().stream()
                .map(entry -> new RequestedLine(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private BigDecimal resolveUnitPrice(StoreProduct storeProduct) {
        if (storeProduct.getSalePrice() != null && storeProduct.getSalePrice().compareTo(BigDecimal.ZERO) > 0) {
            return storeProduct.getSalePrice();
        }
        if (storeProduct.getProduct().getMsrpPrice() != null) {
            return storeProduct.getProduct().getMsrpPrice();
        }
        return BigDecimal.ZERO;
    }

    public record RequestedLine(Long productId, int qty) {
        public RequestedLine {
            Objects.requireNonNull(productId, "productId는 필수입니다.");
            if (qty <= 0) {
                throw new IllegalArgumentException("qty는 1 이상이어야 합니다.");
            }
        }
    }

    public record AllocatedLine(Long productId, String productName, int qty, BigDecimal unitPrice) {
    }

    public record AllocationResult(Warehouse warehouse, List<AllocatedLine> lines, BigDecimal totalAmount) {
    }
}

