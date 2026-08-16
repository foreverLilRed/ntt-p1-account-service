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
    private Opening opening = new Opening();
    private Transactions transactions = new Transactions();
    private Vip vip = new Vip();
    private Pyme pyme = new Pyme();

    @Data
    public static class Checking {
        private BigDecimal maintenanceFee = new BigDecimal("10.00");
    }

    @Data
    public static class Savings {
        private Integer monthlyMovementLimit = 5;
    }

    @Data
    public static class Opening {
        private BigDecimal minimumAmount = BigDecimal.ZERO;
    }

    @Data
    public static class Transactions {
        private Integer freeMonthly = 5;
        private BigDecimal commissionFee = new BigDecimal("2.50");
    }

    @Data
    public static class Vip {
        private BigDecimal minDailyAverage = new BigDecimal("500.00");
    }

    @Data
    public static class Pyme {
        private BigDecimal maintenanceFee = BigDecimal.ZERO;
    }
}
