package com.forerp.erp;

import com.forerp.erp.product.domain.Product;
import com.forerp.erp.product.domain.ProductCategory;
import com.forerp.erp.product.domain.ProductStatus;
import com.forerp.erp.product.repository.ProductCategoryRepository;
import com.forerp.erp.product.repository.ProductRepository;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.store.domain.StoreStatus;
import com.forerp.erp.store.domain.StoreType;
import com.forerp.erp.store.repository.StoreRepository;
import com.forerp.erp.storeproduct.service.StoreProductSyncService;
import com.forerp.erp.supplier.domain.Supplier;
import com.forerp.erp.supplier.repository.SupplierRepository;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.domain.UserRole;
import com.forerp.erp.user.repository.UserRepository;
import com.forerp.erp.warehouse.domain.Warehouse;
import com.forerp.erp.warehouse.repository.WarehouseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=update")
@ActiveProfiles("local")
class LocalSampleDataSeeder {

    private static final String DEFAULT_PASSWORD = "1234";

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StoreProductSyncService storeProductSyncService;

    @Test
    @Transactional
    @Commit
    void seedLocalSampleData() {
        ProductCategory burger = ensureCategory(
                "BURGER",
                "버거",
                "https://images.pexels.com/photos/6088519/pexels-photo-6088519.jpeg"
        );
        ProductCategory side = ensureCategory(
                "SIDE",
                "사이드",
                "https://images.pexels.com/photos/1211887/pexels-photo-1211887.jpeg"
        );
        ProductCategory juice = ensureCategory(
                "JUICE",
                "음료",
                "https://images.pexels.com/photos/10248657/pexels-photo-10248657.jpeg"
        );

        ensureProduct(
                "PRD000001",
                "치즈버거",
                burger,
                new BigDecimal("6500"),
                "https://images.pexels.com/photos/29819319/pexels-photo-29819319.jpeg"
        );
        ensureProduct(
                "PRD000002",
                "새우버거",
                burger,
                new BigDecimal("6900"),
                "https://images.pexels.com/photos/5860588/pexels-photo-5860588.jpeg"
        );
        ensureProduct(
                "PRD000003",
                "감자튀김",
                side,
                new BigDecimal("3200"),
                "https://images.pexels.com/photos/115740/pexels-photo-115740.jpeg"
        );
        ensureProduct(
                "PRD000004",
                "아보카도 샐러드",
                side,
                new BigDecimal("4800"),
                "https://images.pexels.com/photos/1213710/pexels-photo-1213710.jpeg"
        );
        ensureProduct(
                "PRD000005",
                "콜라",
                juice,
                new BigDecimal("2000"),
                "https://images.pexels.com/photos/9706990/pexels-photo-9706990.jpeg"
        );
        ensureProduct(
                "PRD000006",
                "제로콜라",
                juice,
                new BigDecimal("2000"),
                "https://images.pexels.com/photos/10902848/pexels-photo-10902848.jpeg"
        );

        Store headquarters = ensureStore(
                "000",
                "본사",
                StoreType.HQ,
                "02-6000-0000",
                "서울특별시 중구 세종대로 110"
        );
        Store sogang = ensureStore(
                "001",
                "서강대점",
                StoreType.STORE,
                "02-3000-1001",
                "서울특별시 마포구 백범로 35"
        );
        Store hongdae = ensureStore(
                "002",
                "홍대점",
                StoreType.STORE,
                "02-3000-1002",
                "서울특별시 마포구 와우산로 94"
        );

        Warehouse sub001 = ensureWarehouse(
                sogang,
                "SUB_001",
                "SUB_001",
                "서울특별시 마포구 백범로 37"
        );
        Warehouse sub002 = ensureWarehouse(
                sogang,
                "SUB_002",
                "SUB_002",
                "서울특별시 마포구 백범로 39"
        );
        Warehouse sub003 = ensureWarehouse(
                hongdae,
                "SUB_003",
                "SUB_003",
                "서울특별시 마포구 와우산로 96"
        );
        Warehouse sub004 = ensureWarehouse(
                hongdae,
                "SUB_004",
                "SUB_004",
                "서울특별시 마포구 와우산로 98"
        );

        ensureUser("hqchoi", "0001", "최본사", "010-0000-0001", headquarters, UserRole.HQ_ADMIN);
        ensureUser("sg-manager-kim", "1029", "김서강", "010-1111-2222", sogang, UserRole.STORE_ADMIN);
        ensureUser("sg-manager-park", "1928", "박서강", "010-1111-3333", sogang, UserRole.STORE_ADMIN);
        ensureUser("sg-hall-choi", "1119", "최서강", "010-1111-4444", sogang, UserRole.STORE_HALL_STAFF);
        ensureUser("sg-kitchen-lee", "1118", "이서강", "010-1111-5555", sogang, UserRole.STORE_KITCHEN_STAFF);
        ensureUser("hd-manager-kim", "2345", "김홍대", "010-2222-3333", hongdae, UserRole.STORE_ADMIN);

        ensureSupplier(
                "한빛푸드",
                "한도윤",
                "010-3333-4444",
                "sales@hanbitfood.co.kr",
                "서울특별시 마포구 월드컵북로 21"
        );
        ensureSupplier(
                "오션비버리지",
                "오세리",
                "010-5555-6666",
                "biz@oceanbev.co.kr",
                "서울특별시 서대문구 연세로 42"
        );

        List.of(sub001, sub002, sub003, sub004)
                .forEach(storeProductSyncService::syncActiveProductsToWarehouse);
    }

    private ProductCategory ensureCategory(String code, String name, String imageUrl) {
        return productCategoryRepository.findByCode(code)
                .orElseGet(() -> productCategoryRepository.save(
                        ProductCategory.builder()
                                .code(code)
                                .name(name)
                                .imageUrl(imageUrl)
                                .build()
                ));
    }

    private Product ensureProduct(
            String sku,
            String name,
            ProductCategory category,
            BigDecimal price,
            String imageUrl
    ) {
        return productRepository.findBySku(sku)
                .orElseGet(() -> productRepository.save(
                        Product.builder()
                                .sku(sku)
                                .name(name)
                                .category(category)
                                .msrpPrice(price)
                                .status(ProductStatus.ACTIVE)
                                .imageUrl(imageUrl)
                                .build()
                ));
    }

    private Store ensureStore(
            String storeCode,
            String storeName,
            StoreType storeType,
            String phone,
            String address
    ) {
        return storeRepository.findAll().stream()
                .filter(store -> storeCode.equals(store.getStoreCode()))
                .findFirst()
                .map(existing -> {
                    existing.updateInfo(storeName, phone, address);
                    return existing;
                })
                .orElseGet(() -> storeRepository.save(
                        Store.builder()
                                .storeCode(storeCode)
                                .name(storeName)
                                .storeType(storeType)
                                .status(StoreStatus.OPEN)
                                .phone(phone)
                                .address(address)
                                .build()
                ));
    }

    private Warehouse ensureWarehouse(Store store, String code, String name, String address) {
        return warehouseRepository.findByStore_Id(store.getId()).stream()
                .filter(warehouse -> code.equalsIgnoreCase(warehouse.getCode()))
                .findFirst()
                .map(existing -> {
                    existing.updateInfo(code, name, address, true);
                    return existing;
                })
                .orElseGet(() -> warehouseRepository.save(
                        Warehouse.create(store, code, name, address)
                ));
    }

    private User ensureUser(
            String loginId,
            String employeeCode,
            String name,
            String phoneNumber,
            Store store,
            UserRole role
    ) {
        return userRepository.findByLoginId(loginId)
                .map(existing -> {
                    existing.updateInfo(name, phoneNumber, null, store, role, null);
                    return existing;
                })
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .loginId(loginId)
                                .employeeCode(employeeCode)
                                .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                                .name(name)
                                .phoneNumber(phoneNumber)
                                .store(store)
                                .role(role)
                                .build()
                ));
    }

    private Supplier ensureSupplier(
            String name,
            String contactName,
            String contactPhone,
            String contactEmail,
            String address
    ) {
        return supplierRepository.findAll().stream()
                .filter(supplier -> supplier.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> supplierRepository.save(
                        Supplier.create(name, contactName, contactPhone, contactEmail, address)
                ));
    }
}