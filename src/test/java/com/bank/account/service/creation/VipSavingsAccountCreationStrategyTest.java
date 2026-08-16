package com.bank.account.service.creation;

import com.bank.account.config.AccountProperties;
import com.bank.account.dto.AccountRequest;
import com.bank.account.model.Account;
import com.bank.account.model.AccountProductVariant;
import com.bank.account.model.AccountType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VipSavingsAccountCreationStrategyTest {

    @Test
    void buildsVipSavingsWithDailyAverage() {
        AccountProperties properties = new AccountProperties();
        properties.getVip().setMinDailyAverage(new BigDecimal("500.00"));
        VipSavingsAccountCreationStrategy strategy = new VipSavingsAccountCreationStrategy(properties);
        Account account = strategy.buildEntity(AccountRequest.builder()
                .customerId("c1")
                .accountType(AccountType.SAVINGS)
                .build());
        assertEquals(AccountProductVariant.VIP, account.getProductVariant());
        assertEquals(new BigDecimal("500.00"), account.getMinDailyAverageBalance());
        assertEquals(BigDecimal.ZERO, account.getMaintenanceFee());
    }
}
