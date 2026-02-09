package com.forerp.erp.user.repository;

import com.forerp.erp.store.domain.Store;
import com.forerp.erp.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    Optional<User> findByStore_IdAndEmployeeCode(Long storeId, String employeeCode);

    Optional<User> findByStore_StoreCodeAndEmployeeCode(String storeCode, String employeeCode);

    long countByStore(Store store);

    boolean existsByLoginId(String loginId);

    boolean existsByEmployeeCode(String employeeCode);
}