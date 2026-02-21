package com.forerp.erp.product.service;

import com.forerp.erp.common.query.QueryParamParser;
import com.forerp.erp.product.domain.Product;
import com.forerp.erp.product.domain.ProductBundle;
import com.forerp.erp.product.domain.ProductCategory;
import com.forerp.erp.product.domain.ProductStatus;
import com.forerp.erp.product.dto.ProductBundleCandidateResponseDto;
import com.forerp.erp.product.dto.ProductBundleCreateRequestDto;
import com.forerp.erp.product.dto.ProductBundleCreateResponseDto;
import com.forerp.erp.product.dto.ProductCreateRequestDto;
import com.forerp.erp.product.dto.ProductCreateResponseDto;
import com.forerp.erp.product.dto.ProductDto;
import com.forerp.erp.product.dto.ProductListResponseDto;
import com.forerp.erp.product.repository.ProductBundleRepository;
import com.forerp.erp.product.repository.ProductCategoryRepository;
import com.forerp.erp.product.repository.ProductRepository;
import com.forerp.erp.storeproduct.service.StoreProductSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final String SET_CATEGORY_CODE = "SET";
    private static final String SET_CATEGORY_NAME = "세트";
    private static final String SET_CATEGORY_DESCRIPTION = "세트 메뉴";
    private static final String SET_CATEGORY_IMAGE_URL = "https://images.pexels.com/photos/54455/cook-food-kitchen-eat-54455.jpeg";

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductBundleRepository productBundleRepository;
    private final StoreProductSyncService storeProductSyncService;

    @Transactional
    public ProductCreateResponseDto createProduct(ProductCreateRequestDto request) {
        ProductCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("議댁옱?섏? ?딅뒗 移댄뀒怨좊━?낅땲??"));

        Product product = Product.builder()
                .name(request.getName())
                .category(category)
                .description(trimToNull(request.getDescription()))
                .imageUrl(trimToNull(request.getImageUrl()))
                .msrpPrice(request.getPrice())
                .status(ProductStatus.ACTIVE)
                .sku("TEMP")
                .build();

        productRepository.save(product);

        String sku = generateSku(product.getId());
        product.updateSku(sku);
        productRepository.save(product);
        storeProductSyncService.syncActiveProductToActiveWarehouses(product);

        return new ProductCreateResponseDto(product.getId(), sku);
    }

    @Transactional
    public ProductBundleCreateResponseDto createBundleProduct(ProductBundleCreateRequestDto request) {
        Map<Long, Integer> componentQtyByProductId = normalizeBundleItems(request.getItems());
        List<Product> componentProducts = productRepository.findAllById(componentQtyByProductId.keySet());

        if (componentProducts.size() != componentQtyByProductId.size()) {
            throw new IllegalArgumentException("臾띠쓬?곹뭹 援ъ꽦??以?議댁옱?섏? ?딅뒗 ?곹뭹???덉뒿?덈떎.");
        }

        Map<Long, Product> componentById = componentProducts.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        for (Product component : componentProducts) {
            if (component.getStatus() != ProductStatus.ACTIVE) {
                throw new IllegalArgumentException("?쒖꽦 ?곹깭???곹뭹留?援ъ꽦?덉쑝濡??좏깮?????덉뒿?덈떎.");
            }
            if (component.getCategory() != null
                    && SET_CATEGORY_CODE.equalsIgnoreCase(component.getCategory().getCode())) {
                throw new IllegalArgumentException("?명듃 ?곹뭹? 臾띠쓬 援ъ꽦?덉쑝濡??ъ슜?????놁뒿?덈떎.");
            }
        }

        ProductCategory setCategory = ensureSetCategory();

        Product setProduct = Product.builder()
                .name(request.getName().trim())
                .category(setCategory)
                .description(trimToNull(request.getDescription()))
                .imageUrl(trimToNull(request.getImageUrl()))
                .msrpPrice(request.getSetPrice())
                .status(ProductStatus.ACTIVE)
                .sku("TEMP")
                .build();

        productRepository.save(setProduct);

        String sku = generateSku(setProduct.getId());
        setProduct.updateSku(sku);
        productRepository.save(setProduct);

        ProductBundle bundle = ProductBundle.create(setProduct, request.getDiscountRate());
        componentQtyByProductId.forEach((productId, quantity) -> {
            Product component = componentById.get(productId);
            bundle.addItem(component, quantity);
        });
        productBundleRepository.save(bundle);

        storeProductSyncService.syncActiveProductToActiveWarehouses(setProduct);

        return new ProductBundleCreateResponseDto(bundle.getId(), setProduct.getId(), sku);
    }

    @Transactional(readOnly = true)
    public List<ProductBundleCandidateResponseDto> getBundleCandidates() {
        return productRepository
                .findByStatusAndCategory_CodeNotOrderByNameAsc(ProductStatus.ACTIVE, SET_CATEGORY_CODE)
                .stream()
                .map(product -> new ProductBundleCandidateResponseDto(
                        product.getId(),
                        product.getSku(),
                        product.getName(),
                        product.getCategory() == null ? null : product.getCategory().getCode(),
                        product.getCategory() == null ? null : product.getCategory().getName(),
                        product.getMsrpPrice(),
                        product.getImageUrl()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ProductListResponseDto> getAllProducts(
            String productKeyword,
            String name,
            String sku,
            String status,
            Pageable pageable
    ) {
        ProductStatus productStatus = QueryParamParser.parseEnumOrNull(status, ProductStatus.class, "status");

        return productRepository.search(normalize(productKeyword), normalize(name), normalize(sku), productStatus, pageable)
                .map(page -> new ProductListResponseDto(
                        page.getId(),
                        page.getSku(),
                        page.getName(),
                        page.getCategory().getName(),
                        page.getMsrpPrice(),
                        page.getStatus()
                ));
    }

    @Transactional(readOnly = true)
    public ProductDto.DetailResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("?곹뭹??李얠쓣 ???놁뒿?덈떎."));
        return new ProductDto.DetailResponse(product);
    }

    @Transactional
    public ProductDto.DetailResponse updateProduct(Long id, ProductDto.UpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("?곹뭹??李얠쓣 ???놁뒿?덈떎."));

        if (productBundleRepository.existsByProduct_Id(product.getId())
                && request.getCategoryId() != null
                && !request.getCategoryId().equals(product.getCategory().getId())) {
            throw new IllegalArgumentException("臾띠쓬?곹뭹??移댄뀒怨좊━???명듃(SET)濡?怨좎젙?⑸땲??");
        }

        ProductCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("議댁옱?섏? ?딅뒗 移댄뀒怨좊━?낅땲??"));
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

    @Transactional
    public void discontinueProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("?곹뭹??李얠쓣 ???놁뒿?덈떎."));
        product.discontinue();
    }

    @Transactional
    public void reactivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("?곹뭹??李얠쓣 ???놁뒿?덈떎."));
        if (product.getStatus() != ProductStatus.DISCONTINUED) {
            throw new IllegalArgumentException("?먮ℓ 以묒? 泥섎━???곹뭹留??ы뙋留ㅽ븷 ???덉뒿?덈떎.");
        }
        product.reactivate();
        storeProductSyncService.syncActiveProductToActiveWarehouses(product);
    }

    private ProductCategory ensureSetCategory() {
        return categoryRepository.findByCode(SET_CATEGORY_CODE)
                .map(existing -> {
                    existing.update(
                            SET_CATEGORY_CODE,
                            SET_CATEGORY_NAME,
                            SET_CATEGORY_DESCRIPTION,
                            SET_CATEGORY_IMAGE_URL,
                            true
                    );
                    return existing;
                })
                .orElseGet(() -> categoryRepository.save(ProductCategory.builder()
                        .code(SET_CATEGORY_CODE)
                        .name(SET_CATEGORY_NAME)
                        .description(SET_CATEGORY_DESCRIPTION)
                        .imageUrl(SET_CATEGORY_IMAGE_URL)
                        .active(true)
                        .build()));
    }

    private Map<Long, Integer> normalizeBundleItems(List<ProductBundleCreateRequestDto.BundleItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("臾띠쓬?곹뭹 援ъ꽦?덉? 理쒖냼 1媛??댁긽?댁뼱???⑸땲??");
        }

        Map<Long, Integer> merged = new LinkedHashMap<>();
        for (ProductBundleCreateRequestDto.BundleItem item : items) {
            if (item.getProductId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("援ъ꽦???뺣낫媛 ?щ컮瑜댁? ?딆뒿?덈떎.");
            }
            merged.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }
        return merged;
    }

    private String generateSku(Long productId) {
        return String.format("PRD%d%06d", Year.now().getValue(), productId);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimToNull(String value) {
        return normalize(value);
    }
}
