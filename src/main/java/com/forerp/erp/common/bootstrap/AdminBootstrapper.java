package com.forerp.erp.common.bootstrap;

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
                    if (hqAddress != null || hqPhone != null) {
                        String nextAddress = hqAddress != null ? hqAddress : existing.getAddress();
                        String nextPhone = hqPhone != null ? hqPhone : existing.getPhone();
                        existing.updateInfo(
                                null,
                                nextPhone,
                                nextAddress,
                                existing.getLatitude(),
                                existing.getLongitude()
                        );
                    }
                    return existing;
                })
                .orElseGet(() -> storeRepository.save(
                        Store.builder()
                                .name(HQ_STORE_NAME)
                                .storeType(StoreType.HQ)
                                .status(StoreStatus.OPEN)
                                .storeCode(props.getStoreCode())
                                .address(hqAddress)
                                .phone(hqPhone)
                                .build()
                ));
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
