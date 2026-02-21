package com.forerp.erp.common.seed;

import com.forerp.erp.common.location.KakaoAddressGeocodingService;
import com.forerp.erp.product.domain.Product;
import com.forerp.erp.product.domain.ProductCategory;
import com.forerp.erp.product.domain.ProductStatus;
import com.forerp.erp.product.repository.ProductCategoryRepository;
import com.forerp.erp.product.repository.ProductRepository;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.store.domain.StoreStatus;
import com.forerp.erp.store.domain.StoreType;
import com.forerp.erp.store.repository.StoreRepository;
import com.forerp.erp.storeproduct.domain.SaleStatus;
import com.forerp.erp.storeproduct.domain.StoreProduct;
import com.forerp.erp.storeproduct.repository.StoreProductRepository;
import com.forerp.erp.storeproduct.service.StoreProductSyncService;
import com.forerp.erp.supplier.domain.Supplier;
import com.forerp.erp.supplier.repository.SupplierRepository;
import com.forerp.erp.salary.domain.EmploymentType;
import com.forerp.erp.salary.domain.Salary;
import com.forerp.erp.salary.repository.SalaryRepository;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.domain.UserRole;
import com.forerp.erp.user.domain.UserStatus;
import com.forerp.erp.user.repository.UserRepository;
import com.forerp.erp.warehouse.domain.Warehouse;
import com.forerp.erp.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Profile("local")
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
@Transactional
@ConditionalOnProperty(prefix = "app.seed.sample", name = "enabled", havingValue = "true")
public class LocalSampleDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LocalSampleDataSeeder.class);

    private final StoreRepository storeRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;
    private final StoreProductRepository storeProductRepository;
    private final StoreProductSyncService storeProductSyncService;
    private final UserRepository userRepository;
    private final SalaryRepository salaryRepository;
    private final SupplierRepository supplierRepository;
    private final PasswordEncoder passwordEncoder;
    private final KakaoAddressGeocodingService geocodingService;

    @Value("${app.seed.sample.mode:upsert}")
    private String mode;

    @Value("${app.seed.sample.default-password:1111}")
    private String defaultPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (!"upsert".equalsIgnoreCase(mode)) {
            log.info("Skip sample seeding. unsupported mode={}", mode);
            return;
        }

        Store hqStore = ensureHqStore();
        Store sogangStore = upsertStore("101", "서강대점", "02-330-0101", "서울 마포구 서강로 137");
        Store hongdaeStore = upsertStore("102", "홍대점", "02-330-0202", "서울 마포구 양화로 165");

        Warehouse sub001 = upsertWarehouse(sogangStore, "SUB_001", "서강대점 제1창고", "서울 마포구 서강로 131");
        Warehouse sub002 = upsertWarehouse(sogangStore, "SUB_002", "서강대점 제2창고", "서울 마포구 백범로 23");
        Warehouse sub003 = upsertWarehouse(hongdaeStore, "SUB_003", "홍대점 제1창고", "서울 마포구 양화로 156");

        ProductCategory burger = upsertCategory(
                "BURGER",
                "버거",
                "버거 카테고리",
                "https://images.pexels.com/photos/6088519/pexels-photo-6088519.jpeg"
        );
        ProductCategory side = upsertCategory(
                "SIDE",
                "사이드",
                "사이드 카테고리",
                "https://images.pexels.com/photos/1211887/pexels-photo-1211887.jpeg"
        );
        ProductCategory juice = upsertCategory(
                "JUICE",
                "음료",
                "음료 카테고리",
                "https://images.pexels.com/photos/10248657/pexels-photo-10248657.jpeg"
        );

        Product bulgogiBurger = upsertProduct(
                "불고기버거",
                burger,
                "불고기 소스와 패티가 조화로운 버거",
                "https://images.pexels.com/photos/29819319/pexels-photo-29819319.jpeg",
                new BigDecimal("5900")
        );
        Product shrimpBurger = upsertProduct(
                "새우버거",
                burger,
                "바삭한 새우 패티 버거",
                "https://images.pexels.com/photos/5860588/pexels-photo-5860588.jpeg",
                new BigDecimal("6200")
        );
        Product fries = upsertProduct(
                "감자튀김",
                side,
                "바삭한 프렌치 프라이",
                "https://images.pexels.com/photos/115740/pexels-photo-115740.jpeg",
                new BigDecimal("2500")
        );
        Product avocadoSalad = upsertProduct(
                "아보카도 샐러드",
                side,
                "신선한 아보카도 샐러드",
                "https://images.pexels.com/photos/1213710/pexels-photo-1213710.jpeg",
                new BigDecimal("4900")
        );
        Product cola = upsertProduct(
                "콜라",
                juice,
                "탄산음료",
                "https://images.pexels.com/photos/9706990/pexels-photo-9706990.jpeg",
                new BigDecimal("2000")
        );
        Product zeroCola = upsertProduct(
                "제로콜라",
                juice,
                "제로 탄산음료",
                "https://images.pexels.com/photos/10902848/pexels-photo-10902848.jpeg",
                new BigDecimal("2000")
        );

        storeProductSyncService.syncActiveProductsForStore(null, null);

        upsertStock(sub001, bulgogiBurger, 45);
        upsertStock(sub001, shrimpBurger, 40);
        upsertStock(sub001, fries, 65);
        upsertStock(sub001, avocadoSalad, 30);
        upsertStock(sub001, cola, 90);
        upsertStock(sub001, zeroCola, 85);

        upsertStock(sub002, bulgogiBurger, 30);
        upsertStock(sub002, shrimpBurger, 28);
        upsertStock(sub002, fries, 45);
        upsertStock(sub002, avocadoSalad, 22);
        upsertStock(sub002, cola, 70);
        upsertStock(sub002, zeroCola, 68);

        upsertStock(sub003, bulgogiBurger, 50);
        upsertStock(sub003, shrimpBurger, 44);
        upsertStock(sub003, fries, 70);
        upsertStock(sub003, avocadoSalad, 32);
        upsertStock(sub003, cola, 95);
        upsertStock(sub003, zeroCola, 90);

        User hqManager = upsertUser(hqStore, "hqchoi", "0001", "최본사", UserRole.HQ_ADMIN, "010-0001-0001");
        User sogangManagerKim = upsertUser(sogangStore, "sgkim", "1029", "김서강", UserRole.STORE_ADMIN, "010-1029-1029");
        User sogangManagerPark = upsertUser(sogangStore, "sgchoi", "1031", "최서강", UserRole.STORE_ADMIN, "010-1031-1031");
        User sogangHallStaff = upsertUser(sogangStore, "sghall", "1111", "박서강", UserRole.STORE_HALL_STAFF, "010-1111-1111");
        User sogangKitchenStaff = upsertUser(sogangStore, "sgkitchen", "1112", "이서강", UserRole.STORE_KITCHEN_STAFF, "010-1112-1112");
        User sogangHallStaffExtra = upsertUser(sogangStore, "sghall2", "1113", "정서강", UserRole.STORE_HALL_STAFF, "010-1113-1113");
        User sogangKitchenStaffExtra = upsertUser(sogangStore, "sgkitchen2", "1114", "한서강", UserRole.STORE_KITCHEN_STAFF, "010-1114-1114");
        User hongdaeManager = upsertUser(hongdaeStore, "hdkim", "1209", "김홍대", UserRole.STORE_ADMIN, "010-1209-1209");

        upsertSalary(hqManager, EmploymentType.MONTHLY, null, new BigDecimal("5000000"), resolveSeedPaymentDate(hqManager, 5));
        upsertSalary(sogangManagerKim, EmploymentType.MONTHLY, null, new BigDecimal("3000000"), resolveSeedPaymentDate(sogangManagerKim, 8));
        upsertSalary(sogangManagerPark, EmploymentType.MONTHLY, null, new BigDecimal("3000000"), resolveSeedPaymentDate(sogangManagerPark, 10));
        upsertSalary(hongdaeManager, EmploymentType.MONTHLY, null, new BigDecimal("3000000"), resolveSeedPaymentDate(hongdaeManager, 12));
        upsertSalary(sogangHallStaff, EmploymentType.HOURLY, new BigDecimal("12000"), null, resolveSeedPaymentDate(sogangHallStaff, 15));
        upsertSalary(sogangKitchenStaff, EmploymentType.HOURLY, new BigDecimal("12000"), null, resolveSeedPaymentDate(sogangKitchenStaff, 18));
        upsertSalary(sogangHallStaffExtra, EmploymentType.HOURLY, new BigDecimal("12000"), null, resolveSeedPaymentDate(sogangHallStaffExtra, 20));
        upsertSalary(sogangKitchenStaffExtra, EmploymentType.HOURLY, new BigDecimal("12000"), null, resolveSeedPaymentDate(sogangKitchenStaffExtra, 22));

        upsertSupplier(
                "신선푸드상사",
                "최유통",
                "02-2000-1001",
                "fresh@forerp.local",
                "서울 마포구 월드컵북로 12"
        );
        upsertSupplier(
                "그린베지유통",
                "정채소",
                "02-2000-1002",
                "green@forerp.local",
                "서울 서대문구 연세로 42"
        );
        upsertSupplier(
                "드링크웍스",
                "한음료",
                "02-2000-1003",
                "drink@forerp.local",
                "서울 마포구 독막로 45"
        );

        log.info("Local sample data seeding completed.");
    }

    private Store ensureHqStore() {
        Optional<Store> hqByCode = storeRepository.findByStoreCode("000");
        if (hqByCode.isPresent()) {
            return hqByCode.get();
        }

        return storeRepository.findFirstByStoreType(StoreType.HQ)
                .orElseGet(() -> storeRepository.save(
                        Store.builder()
                                .storeCode("000")
                                .name("본사")
                                .storeType(StoreType.HQ)
                                .status(StoreStatus.OPEN)
                                .address("서울 강남구 테헤란로1길")
                                .phone("02-0000-0000")
                                .build()
                ));
    }

    private Store upsertStore(String storeCode, String name, String phone, String address) {
        Coordinates coordinates = resolveCoordinates(address);

        return storeRepository.findByStoreCode(storeCode)
                .map(existing -> {
                    existing.updateInfo(name, phone, address, coordinates.latitude(), coordinates.longitude());
                    if (existing.getStatus() != StoreStatus.OPEN) {
                        existing.open();
                    }
                    return existing;
                })
                .orElseGet(() -> storeRepository.save(
                        Store.builder()
                                .storeCode(storeCode)
                                .name(name)
                                .storeType(StoreType.STORE)
                                .status(StoreStatus.OPEN)
                                .phone(phone)
                                .address(address)
                                .latitude(coordinates.latitude())
                                .longitude(coordinates.longitude())
                                .build()
                ));
    }

    private Warehouse upsertWarehouse(Store store, String code, String name, String address) {
        Coordinates coordinates = resolveCoordinates(address);

        return warehouseRepository.findByStore_IdAndCode(store.getId(), code)
                .map(existing -> {
                    existing.updateInfo(code, name, address, coordinates.latitude(), coordinates.longitude(), true);
                    return existing;
                })
                .orElseGet(() -> warehouseRepository.save(
                        Warehouse.create(
                                store,
                                code,
                                name,
                                address,
                                coordinates.latitude(),
                                coordinates.longitude()
                        )
                ));
    }

    private ProductCategory upsertCategory(String code, String name, String description, String imageUrl) {
        return productCategoryRepository.findByCode(code)
                .map(existing -> {
                    existing.update(code, name, description, imageUrl, true);
                    return existing;
                })
                .orElseGet(() -> productCategoryRepository.save(
                        ProductCategory.builder()
                                .code(code)
                                .name(name)
                                .description(description)
                                .imageUrl(imageUrl)
                                .active(true)
                                .build()
                ));
    }

    private Product upsertProduct(
            String name,
            ProductCategory category,
            String description,
            String imageUrl,
            BigDecimal price
    ) {
        return productRepository.findFirstByName(name)
                .map(existing -> {
                    existing.update(name, category, price, description, imageUrl);
                    if (existing.getStatus() == ProductStatus.DISCONTINUED) {
                        existing.reactivate();
                    }
                    if (existing.getSku() == null || existing.getSku().isBlank()) {
                        existing.updateSku(generateSku(existing.getId()));
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    Product product = Product.builder()
                            .name(name)
                            .category(category)
                            .description(description)
                            .imageUrl(imageUrl)
                            .msrpPrice(price)
                            .status(ProductStatus.ACTIVE)
                            .sku("TEMP")
                            .build();

                    productRepository.save(product);
                    product.updateSku(generateSku(product.getId()));
                    return productRepository.save(product);
                });
    }

    private void upsertStock(Warehouse warehouse, Product product, int quantity) {
        Store store = warehouse.getStore();
        StoreProduct storeProduct = storeProductRepository
                .findByStore_IdAndWarehouse_IdAndProduct_Id(store.getId(), warehouse.getId(), product.getId())
                .orElseGet(() -> storeProductRepository.save(
                        StoreProduct.create(store, warehouse, product)
                ));

        storeProduct.changeSaleStatus(SaleStatus.ON);
        storeProduct.changeSalePrice(product.getMsrpPrice());
        storeProduct.updateStock(quantity);
    }

    private User upsertUser(
            Store store,
            String loginId,
            String employeeCode,
            String name,
            UserRole role,
            String phoneNumber
    ) {
        String passwordHash = passwordEncoder.encode(defaultPassword);

        return userRepository.findByLoginId(loginId)
                .map(existing -> {
                    existing.updateInfo(
                            name,
                            phoneNumber,
                            passwordHash,
                            store,
                            role,
                            UserStatus.ACTIVE
                    );
                    return existing;
                })
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .loginId(loginId)
                                .employeeCode(employeeCode)
                                .passwordHash(passwordHash)
                                .name(name)
                                .phoneNumber(phoneNumber)
                                .store(store)
                                .role(role)
                                .build()
                ));
    }

    private void upsertSalary(
            User user,
            EmploymentType employmentType,
            BigDecimal hourlyWage,
            BigDecimal monthlySalary,
            LocalDate paymentDate
    ) {
        if (user == null || user.getId() == null) {
            return;
        }

        salaryRepository.findByUser_Id(user.getId())
                .ifPresentOrElse(
                        salary -> salary.update(employmentType, hourlyWage, monthlySalary, paymentDate),
                        () -> salaryRepository.save(
                                Salary.builder()
                                        .user(user)
                                        .employmentType(employmentType)
                                        .hourlyWage(hourlyWage)
                                        .monthlySalary(monthlySalary)
                                        .paymentDate(paymentDate)
                                        .build()
                        )
                );
    }

    private LocalDate resolveSeedPaymentDate(User user, int daysAfterJoin) {
        LocalDate fallback = LocalDate.now().plusDays(Math.max(1, daysAfterJoin));
        if (user == null || user.getCreatedAt() == null) {
            return fallback;
        }

        LocalDate joinDate = user.getCreatedAt().toLocalDate();
        LocalDate maxDate = joinDate.plusMonths(1);
        LocalDate candidate = joinDate.plusDays(Math.max(1, daysAfterJoin));
        if (candidate.isAfter(maxDate)) {
            return maxDate;
        }
        return candidate;
    }

    private void upsertSupplier(
            String name,
            String contactName,
            String contactPhone,
            String contactEmail,
            String address
    ) {
        Coordinates coordinates = resolveCoordinates(address);

        supplierRepository.findFirstByName(name)
                .ifPresentOrElse(existing -> existing.update(
                                name,
                                contactName,
                                contactPhone,
                                contactEmail,
                                address,
                                coordinates.latitude(),
                                coordinates.longitude(),
                                true
                        ),
                        () -> supplierRepository.save(Supplier.create(
                                name,
                                contactName,
                                contactPhone,
                                contactEmail,
                                address,
                                coordinates.latitude(),
                                coordinates.longitude()
                        )));
    }

    private Coordinates resolveCoordinates(String address) {
        return geocodingService.geocode(address)
                .map(geo -> new Coordinates(geo.latitude(), geo.longitude()))
                .orElseGet(() -> new Coordinates(null, null));
    }

    private String generateSku(Long productId) {
        return String.format("PRD%d%06d", Year.now().getValue(), productId);
    }

    private record Coordinates(Double latitude, Double longitude) {
    }
}
