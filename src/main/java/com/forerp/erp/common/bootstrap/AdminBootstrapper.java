package com.forerp.erp.common.bootstrap;

import com.forerp.erp.common.location.KakaoAddressGeocodingService;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.store.domain.StoreStatus;
import com.forerp.erp.store.domain.StoreType;
import com.forerp.erp.store.repository.StoreRepository;
import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@ConditionalOnProperty(
        prefix = "app.bootstrap.admin",
        name = "enabled",
        havingValue = "true"
)
@Component
@RequiredArgsConstructor
@Transactional
public class AdminBootstrapper implements ApplicationRunner {

    private final AdminBootstrapProperties props;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;
    private final KakaoAddressGeocodingService geocodingService;

    private static final String HQ_STORE_NAME = "본사";

    @Override
    public void run(ApplicationArguments args) {
        if (!props.isEnabled()) return;

        Store hqStore = ensureHqStore();
        ensureHqAdmin(hqStore);
    }

    private Store ensureHqStore() {
        String hqAddress = normalize(props.getStoreAddress());
        String hqPhone = normalize(props.getStorePhone());

        return storeRepository.findFirstByStoreType(StoreType.HQ)
                .map(existing -> {
                    String nextAddress = hqAddress != null ? hqAddress : existing.getAddress();
                    String nextPhone = hqPhone != null ? hqPhone : existing.getPhone();
                    Double nextLatitude = existing.getLatitude();
                    Double nextLongitude = existing.getLongitude();

                    boolean shouldGeocode = nextAddress != null
                            && (hqAddress != null || nextLatitude == null || nextLongitude == null);
                    if (shouldGeocode) {
                        var coordinates = geocodingService.geocode(nextAddress);
                        if (coordinates.isPresent()) {
                            nextLatitude = coordinates.get().latitude();
                            nextLongitude = coordinates.get().longitude();
                        }
                    }

                    boolean hasInputChanges = hqAddress != null || hqPhone != null;
                    boolean hasCoordinateChanges = !Objects.equals(nextLatitude, existing.getLatitude())
                            || !Objects.equals(nextLongitude, existing.getLongitude());

                    if (hasInputChanges || hasCoordinateChanges) {
                        existing.updateInfo(
                                null,
                                nextPhone,
                                nextAddress,
                                nextLatitude,
                                nextLongitude
                        );
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    Double latitude = null;
                    Double longitude = null;
                    if (hqAddress != null) {
                        var coordinates = geocodingService.geocode(hqAddress);
                        if (coordinates.isPresent()) {
                            latitude = coordinates.get().latitude();
                            longitude = coordinates.get().longitude();
                        }
                    }

                    return storeRepository.save(
                            Store.builder()
                                    .name(HQ_STORE_NAME)
                                    .storeType(StoreType.HQ)
                                    .status(StoreStatus.OPEN)
                                    .storeCode(props.getStoreCode())
                                    .address(hqAddress)
                                    .phone(hqPhone)
                                    .latitude(latitude)
                                    .longitude(longitude)
                                    .build()
                    );
                });
    }

    private void ensureHqAdmin(Store hqStore) {
        if (userRepository.existsByLoginId(props.getLoginId())) return;

        User admin = User.builder()
                .loginId(props.getLoginId())
                .employeeCode(props.getEmployeeCode())
                .passwordHash(passwordEncoder.encode(props.getPassword()))
                .name(props.getName())
                .phoneNumber(props.getPhoneNumber())
                .store(hqStore)
                .role(props.getRole())
                .build();

        userRepository.save(admin);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
