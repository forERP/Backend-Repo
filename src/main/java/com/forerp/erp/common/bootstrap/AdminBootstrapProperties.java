package com.forerp.erp.common.bootstrap;

import com.forerp.erp.user.domain.UserRole;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@Setter
@ConfigurationProperties(prefix = "app.bootstrap.admin")
public class AdminBootstrapProperties {
    private boolean enabled = true;
    private String loginId;
    private String employeeCode;
    private String password;
    private String name;
    private UserRole role = UserRole.HQ_ADMIN;

    private String storeCode = "000";
}