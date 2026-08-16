package com.bank.account.service.creation;

import com.bank.account.config.AccountProperties;
import com.bank.account.dto.AccountRequest;
import com.bank.account.model.Account;
import com.bank.account.model.AccountProductVariant;
import com.bank.account.model.AccountType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PymeCheckingAccountCreationStrategyTest {

    @Test
    void buildsPymeCheckingWithoutMaintenanceFee() {
        AccountProperties properties = new AccountProperties();
        properties.getPyme().setMaintenanceFee(BigDecimal.ZERO);
        PymeCheckingAccountCreationStrategy strategy = new PymeCheckingAccountCreationStrategy(properties);
        Account account = strategy.buildEntity(AccountRequest.builder()
                .customerId("c1")
                .accountType(AccountType.CHECKING)
                .build());
        assertEquals(AccountProductVariant.PYME, account.getProductVariant());
        assertEquals(0, account.getMaintenanceFee().compareTo(BigDecimal.ZERO));
    }
}
