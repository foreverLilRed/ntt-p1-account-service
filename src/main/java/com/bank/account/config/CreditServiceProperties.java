package com.bank.account.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Externalized URL for credit-service integration.
 */
@Data
@Component
@ConfigurationProperties(prefix = "services.credit")
public class CreditServiceProperties {

    private String baseUrl = "http://localhost:8083";
}
