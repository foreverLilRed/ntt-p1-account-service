package com.bank.account.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Externalized URL for customer-service integration.
 */
@Data
@Component
@ConfigurationProperties(prefix = "services.customer")
public class CustomerServiceProperties {

    private String baseUrl = "http://localhost:8081";
}
