package com.forerp.erp.user.domain;

import com.forerp.erp.store.domain.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "login_id", nullable = false, length = 50, unique = true)
    private String loginId;

    @Column(name = "employee_code", nullable = false, length = 50, unique = true)
    private String employeeCode;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onPrePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = UserStatus.ACTIVE;
    }

    @Builder
    public User(String loginId,
                String employeeCode,
                String passwordHash,
                String name,
                Store store,
                UserRole role) {

        this.loginId = loginId;
        this.employeeCode = employeeCode;
        this.passwordHash = passwordHash;
        this.name = name;
        this.store = store;
        this.role = role;
        this.status = UserStatus.ACTIVE;
    }

    // 회원 정보 수정
    public void updateInfo(String name, String passwordHash, Store store, UserRole role){
        if (name != null) this.name = name;
        if (passwordHash != null) this.passwordHash = passwordHash;
        if (store != null) this.store = store;
        if (role != null) this.role = role;
    }
}