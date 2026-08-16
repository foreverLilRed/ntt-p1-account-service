package com.bank.account.service.creation;

import com.bank.account.dto.AccountRequest;
import com.bank.account.dto.CustomerDto;
import com.bank.account.service.DebtStatusStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OverdueDebtCreationRuleTest {

    @Mock
    private DebtStatusStore store;

    @Test
    void rejectsOverdueCustomer() {
        when(store.hasOverdueDebt("c1")).thenReturn(Mono.just(true));
        OverdueDebtCreationRule rule = new OverdueDebtCreationRule(store);
        StepVerifier.create(rule.validate(CustomerDto.builder().id("c1").build(), new AccountRequest()))
                .expectError()
                .verify();
    }
}
