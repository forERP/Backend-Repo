package com.forerp.erp.user.domain;

import com.forerp.erp.store.domain.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED )
public class User {

    @Id
    @Column(name = "user_id")
    private Long id;

    @Column(name = "login_id", nullable = false, length = 50, unique = true)
    private String loginId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // 어느 매장 소속인지 나타내는 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "permissions")
    private String permission;

    public String getRole(){
        if(this.id==null){
            return null;
        }
        switch (this.id.intValue()){
            case 1:
                return "HQ_ADMIN";
            case 2:
                return "STORE_ADMIN";
            case 3:
                return "STORE_HALL_STAFF";
            case 4:
                return "STORE_KITCHEN_STAFF";
            default:
                return  "UNKNOWN";
        }
    }

    public Set<String> getPermissions(){
        if(this.permission == null || this.permission.isBlank()){
            return Collections.emptySet();
        }
        return Arrays.stream(this.permission.split(",")).collect(Collectors.toSet());
    }

    @PrePersist
    protected void onPrePersist(){
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public User(Long id, String loginId, Store store, String passwordHash, String name,
                Set<String> permission){
        this.id = id;
        this.loginId = loginId;
        this.store = store;
        this.name = name;
        this.passwordHash = passwordHash;
        this.status = UserStatus.ACTIVE;
        if(permission != null && !permission.isEmpty()){
            this.permission= String.join(",", permission);
        }
    }
}