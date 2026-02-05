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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Transactional
public class AdminBootstrapper implements ApplicationRunner {

    private final AdminBootstrapProperties props;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String HQ_STORE_NAME = "HQ";

    @Override
    public void run(ApplicationArguments args) {
        if (!props.isEnabled()) return;

        Store hqStore = ensureHqStore();
        ensureHqAdmin(hqStore);
    }

    private Store ensureHqStore() {
        return storeRepository.findFirstByStoreType(StoreType.HQ)
                .orElseGet(() -> storeRepository.save(
                        Store.builder()
                                .name(HQ_STORE_NAME)
                                .storeType(StoreType.HQ)
                                .status(StoreStatus.OPEN)
                                .storeCode(props.getStoreCode())
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
                .store(hqStore)
                .role(props.getRole())
                .build();

        userRepository.save(admin);
    }
}