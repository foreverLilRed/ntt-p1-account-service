package com.bank.account.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Externalized account product defaults loaded from Config Server.
 */
@Data
@Component
@ConfigurationProperties(prefix = "account")
public class AccountProperties {

    private Checking checking = new Checking();
    private Savings savings = new Savings();

    @Data
    public static class Checking {
        private BigDecimal maintenanceFee = new BigDecimal("10.00");
    }

    @Data
    public static class Savings {
        private Integer monthlyMovementLimit = 5;
    }
}
