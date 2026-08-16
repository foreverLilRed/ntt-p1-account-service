package com.bank.account.service.creation;

import com.bank.account.client.CreditClient;
import com.bank.account.dto.AccountRequest;
import com.bank.account.dto.CustomerDto;
import com.bank.account.exception.BusinessException;
import com.bank.account.model.AccountType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditCardRequirementRuleTest {

    @Mock
    private CreditClient creditClient;

    @InjectMocks
    private CreditCardRequirementRule rule;

    @Test
    void vipSavingsFailsWithoutCard() {
        when(creditClient.hasActiveCreditCard("c1")).thenReturn(Mono.just(false));
        StepVerifier.create(rule.validate(
                        CustomerDto.builder().id("c1").customerProfile("VIP").build(),
                        AccountRequest.builder().customerId("c1").accountType(AccountType.SAVINGS).build()))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void pymeCheckingFailsWithoutCard() {
        when(creditClient.hasActiveCreditCard("c1")).thenReturn(Mono.just(false));
        StepVerifier.create(rule.validate(
                        CustomerDto.builder().id("c1").customerProfile("PYME").build(),
                        AccountRequest.builder().customerId("c1").accountType(AccountType.CHECKING).build()))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void standardProfileDoesNotCallCredit() {
        StepVerifier.create(rule.validate(
                        CustomerDto.builder().id("c1").customerProfile("STANDARD").build(),
                        AccountRequest.builder().customerId("c1").accountType(AccountType.SAVINGS).build()))
                .verifyComplete();
        verify(creditClient, never()).hasActiveCreditCard("c1");
    }
}
