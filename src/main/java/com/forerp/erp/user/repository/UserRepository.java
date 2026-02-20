package com.forerp.erp.user.repository;

import com.forerp.erp.user.domain.User;
import com.forerp.erp.user.domain.UserRole;
import com.forerp.erp.user.domain.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    Optional<User> findByStore_IdAndEmployeeCode(Long storeId, String employeeCode);

    Optional<User> findByStore_StoreCodeAndEmployeeCode(String storeCode, String employeeCode);

    boolean existsByLoginId(String loginId);

    boolean existsByEmployeeCode(String employeeCode);

    @Query("select max(u.employeeCode) from User u")
    String findMaxEmployeeCode();

    @Query("""
        select u from User u
        left join u.store s
        where (:storeName is null or lower(s.name) like lower(concat('%', :storeName, '%')))
          and (:storeCode is null or lower(s.storeCode) like lower(concat('%', :storeCode, '%')))
          and (:name is null or lower(u.name) like lower(concat('%', :name, '%')))
          and (:status is null or u.status = :status)
          and (:role is null or u.role = :role)
          and (:createdFrom is null or u.createdAt >= :createdFrom)
          and (:createdTo is null or u.createdAt < :createdTo)
        """)
    Page<User> search(
            @Param("storeName") String storeName,
            @Param("storeCode") String storeCode,
            @Param("name") String name,
            @Param("status") UserStatus status,
            @Param("role") UserRole role,
            @Param("createdFrom") LocalDateTime createdFrom,
            @Param("createdTo") LocalDateTime createdTo,
            Pageable pageable
    );
}
