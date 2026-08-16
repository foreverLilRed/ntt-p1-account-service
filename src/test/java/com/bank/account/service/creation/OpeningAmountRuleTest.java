package com.bank.account.service.creation;

import com.bank.account.config.AccountProperties;
import com.bank.account.dto.AccountRequest;
import com.bank.account.dto.CustomerDto;
import com.bank.account.exception.BusinessException;
import com.bank.account.model.AccountType;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

class OpeningAmountRuleTest {

    @Test
    void rejectsDepositBelowMinimum() {
        AccountProperties properties = new AccountProperties();
        properties.getOpening().setMinimumAmount(new BigDecimal("100"));
        OpeningAmountRule rule = new OpeningAmountRule(properties);
        AccountRequest request = AccountRequest.builder()
                .customerId("c1")
                .accountType(AccountType.SAVINGS)
                .initialDeposit(new BigDecimal("50"))
                .build();
        StepVerifier.create(rule.validate(CustomerDto.builder().id("c1").build(), request))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void acceptsZeroWhenMinimumIsZero() {
        OpeningAmountRule rule = new OpeningAmountRule(new AccountProperties());
        AccountRequest request = AccountRequest.builder()
                .customerId("c1")
                .accountType(AccountType.SAVINGS)
                .build();
        StepVerifier.create(rule.validate(CustomerDto.builder().id("c1").build(), request))
                .verifyComplete();
    }

    @Test
    void acceptsDepositEqualToMinimum() {
        AccountProperties properties = new AccountProperties();
        properties.getOpening().setMinimumAmount(new BigDecimal("100"));
        OpeningAmountRule rule = new OpeningAmountRule(properties);
        AccountRequest request = AccountRequest.builder()
                .customerId("c1")
                .accountType(AccountType.SAVINGS)
                .initialDeposit(new BigDecimal("100"))
                .build();
        StepVerifier.create(rule.validate(CustomerDto.builder().id("c1").build(), request))
                .verifyComplete();
    }
}
