package com.forerp.erp;

import com.forerp.erp.common.AdminBootstrapProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AdminBootstrapProperties.class)
public class ErpBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(ErpBackendApplication.class, args);
	}
}