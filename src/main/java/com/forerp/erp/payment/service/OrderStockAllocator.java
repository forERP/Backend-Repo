package com.forerp.erp.payment.service;

import com.forerp.erp.product.domain.ProductBundle;
import com.forerp.erp.product.domain.ProductStatus;
import com.forerp.erp.product.repository.ProductBundleRepository;
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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderStockAllocator {

    private final WarehouseRepository warehouseRepository;
    private final StoreProductRepository storeProductRepository;
    private final ProductBundleRepository productBundleRepository;

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

        List<Long> requestedProductIds = deduplicatedLines.stream()
                .map(RequestedLine::productId)
                .distinct()
                .toList();
        Map<Long, ProductBundle> bundleByProductId = findBundleByProductId(requestedProductIds);

        Set<Long> stockProductIds = new LinkedHashSet<>(requestedProductIds);
        bundleByProductId.values().forEach(bundle ->
                bundle.getItems().forEach(item -> stockProductIds.add(item.getComponentProduct().getId()))
        );

        List<Long> warehouseIds = warehouses.stream().map(Warehouse::getId).toList();
        List<StoreProduct> storeProducts = storeProductRepository
                .findByStore_IdAndWarehouse_IdInAndProduct_IdInAndProduct_Status(
                        store.getId(),
                        warehouseIds,
                        new ArrayList<>(stockProductIds),
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
            Map<Long, Integer> requiredStockQtyByProductId = new HashMap<>();
            boolean allSatisfied = true;

            for (RequestedLine requestLine : deduplicatedLines) {
                StoreProduct orderedStoreProduct = stockByProduct.get(requestLine.productId());
                if (orderedStoreProduct == null || orderedStoreProduct.getSaleStatus() != SaleStatus.ON) {
                    allSatisfied = false;
                    break;
                }

                BigDecimal unitPrice = resolveUnitPrice(orderedStoreProduct);
                List<StockDeduction> stockDeductions = resolveLineStockDeductions(
                        requestLine.productId(),
                        bundleByProductId
                );

                for (StockDeduction stockDeduction : stockDeductions) {
                    int requiredQty = stockDeduction.qtyPerUnit() * requestLine.qty();
                    requiredStockQtyByProductId.merge(stockDeduction.productId(), requiredQty, Integer::sum);
                }

                allocatedLines.add(new AllocatedLine(
                        requestLine.productId(),
                        orderedStoreProduct.getProduct().getName(),
                        requestLine.qty(),
                        unitPrice,
                        stockDeductions
                ));
            }

            if (!allSatisfied) {
                continue;
            }

            if (!hasSufficientStock(stockByProduct, requiredStockQtyByProductId)) {
                continue;
            }

            BigDecimal totalAmount = allocatedLines.stream()
                    .map(line -> line.unitPrice().multiply(BigDecimal.valueOf(line.qty())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return new AllocationResult(warehouse, allocatedLines, totalAmount);
        }

        throw new IllegalStateException("선택 가능한 재고가 없습니다. 모든 창고에서 재고가 부족하거나 판매중지 상태입니다.");
    }

    private Map<Long, ProductBundle> findBundleByProductId(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        return productBundleRepository.findByProduct_IdIn(productIds).stream()
                .collect(Collectors.toMap(bundle -> bundle.getProduct().getId(), bundle -> bundle));
    }

    private boolean hasSufficientStock(
            Map<Long, StoreProduct> stockByProduct,
            Map<Long, Integer> requiredStockQtyByProductId
    ) {
        for (Map.Entry<Long, Integer> entry : requiredStockQtyByProductId.entrySet()) {
            StoreProduct stock = stockByProduct.get(entry.getKey());
            if (stock == null || stock.getQuantity() < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    private List<StockDeduction> resolveLineStockDeductions(
            Long requestedProductId,
            Map<Long, ProductBundle> bundleByProductId
    ) {
        ProductBundle bundle = bundleByProductId.get(requestedProductId);
        if (bundle == null || bundle.getItems() == null || bundle.getItems().isEmpty()) {
            return List.of(new StockDeduction(requestedProductId, 1));
        }

        return bundle.getItems().stream()
                .map(item -> new StockDeduction(
                        item.getComponentProduct().getId(),
                        item.getQuantityPerBundle()
                ))
                .toList();
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

    public record StockDeduction(Long productId, int qtyPerUnit) {
        public StockDeduction {
            Objects.requireNonNull(productId, "productId는 필수입니다.");
            if (qtyPerUnit <= 0) {
                throw new IllegalArgumentException("qtyPerUnit은 1 이상이어야 합니다.");
            }
        }
    }

    public record AllocatedLine(
            Long productId,
            String productName,
            int qty,
            BigDecimal unitPrice,
            List<StockDeduction> stockDeductions
    ) {
    }

    public record AllocationResult(Warehouse warehouse, List<AllocatedLine> lines, BigDecimal totalAmount) {
    }
}
