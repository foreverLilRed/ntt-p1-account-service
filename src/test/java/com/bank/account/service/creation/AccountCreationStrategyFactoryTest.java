package com.bank.account.service.creation;

import com.bank.account.dto.AccountRequest;
import com.bank.account.model.AccountType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountCreationStrategyFactoryTest {

    @Test
    void selectsVipStrategyForVipSavings() {
        AccountPropertiesHolder holder = new AccountPropertiesHolder();
        AccountCreationStrategyFactory factory = new AccountCreationStrategyFactory(java.util.List.of(
                new SavingsAccountCreationStrategy(holder.properties()),
                new VipSavingsAccountCreationStrategy(holder.properties())
        ));
        AccountCreationStrategy strategy = factory.resolve(AccountType.SAVINGS, "VIP");
        assertTrue(strategy instanceof VipSavingsAccountCreationStrategy);
        AccountRequest request = AccountRequest.builder()
                .customerId("c1")
                .accountType(AccountType.SAVINGS)
                .build();
        assertTrue(strategy.buildEntity(request).getProductVariant().name().equals("VIP"));
    }

    private static final class AccountPropertiesHolder {
        private com.bank.account.config.AccountProperties properties() {
            return new com.bank.account.config.AccountProperties();
        }
    }
}
