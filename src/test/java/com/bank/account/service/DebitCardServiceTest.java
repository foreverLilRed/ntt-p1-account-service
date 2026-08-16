package com.bank.account.service;

import com.bank.account.dto.DebitCardRequest;
import com.bank.account.exception.BusinessException;
import com.bank.account.model.Account;
import com.bank.account.model.DebitCard;
import com.bank.account.model.DebitCardStatus;
import com.bank.account.repository.AccountRepository;
import com.bank.account.repository.DebitCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebitCardServiceTest {

    @Mock
    private DebitCardRepository debitCardRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountLedger accountLedger;
    @Mock
    private DebtStatusStore debtStatusStore;

    private DebitCardService service;

    @BeforeEach
    void setUp() {
        service = new DebitCardService(debitCardRepository, accountRepository, accountLedger, debtStatusStore);
    }

    @Test
    void createRejectsOverdueCustomer() {
        when(debtStatusStore.hasOverdueDebt("c1")).thenReturn(Mono.just(true));
        DebitCardRequest request = DebitCardRequest.builder()
                .customerId("c1")
                .cardNumber("4111")
                .primaryAccountId("a1")
                .accountIds(List.of("a1"))
                .build();
        assertThrows(BusinessException.class, () -> service.create(request).blockingGet());
    }

    @Test
    void createPersistsCard() {
        when(debtStatusStore.hasOverdueDebt("c1")).thenReturn(Mono.just(false));
        Account account = Account.builder().id("a1").customerId("c1").balance(BigDecimal.TEN).build();
        when(accountRepository.findById("a1")).thenReturn(Mono.just(account));
        when(debitCardRepository.findByCardNumber("4111")).thenReturn(Mono.empty());
        when(debitCardRepository.save(any())).thenAnswer(inv -> {
            DebitCard card = inv.getArgument(0);
            card.setId("d1");
            return Mono.just(card);
        });
        DebitCardRequest request = DebitCardRequest.builder()
                .customerId("c1")
                .cardNumber("4111")
                .primaryAccountId("a1")
                .accountIds(List.of("a1"))
                .build();
        assertEquals("d1", service.create(request).blockingGet().getId());
        assertEquals(DebitCardStatus.ACTIVE, service.create(request).blockingGet().getStatus());
    }
}
