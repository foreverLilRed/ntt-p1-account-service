package com.bank.account.service;

import com.bank.account.exception.BusinessException;
import com.bank.account.model.Account;
import com.bank.account.model.AccountMovement;
import com.bank.account.model.AccountStatus;
import com.bank.account.model.AccountType;
import com.bank.account.model.MovementType;
import com.bank.account.repository.AccountMovementRepository;
import com.bank.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountLedgerTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountMovementRepository movementRepository;

    private AccountLedger ledger;

    @BeforeEach
    void setUp() {
        ledger = new AccountLedger(accountRepository, movementRepository,
                new TransactionCommissionCalculator(), new DailyAverageCalculator());
    }

    @Test
    void rejectsInsufficientFunds() {
        Account account = baseAccount().balance(new BigDecimal("10")).build();
        when(movementRepository.findByAccountIdAndOccurredAtGreaterThanEqualAndOccurredAtLessThan(
                any(), any(), any())).thenReturn(Flux.empty());
        StepVerifier.create(ledger.apply(account, new BigDecimal("20"), MovementType.WITHDRAWAL, null))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void subtractsCommissionFromBalance() {
        Account account = baseAccount()
                .balance(BigDecimal.ZERO)
                .freeMonthlyTransactions(0)
                .transactionCommissionFee(new BigDecimal("2.50"))
                .build();
        when(movementRepository.findByAccountIdAndOccurredAtGreaterThanEqualAndOccurredAtLessThan(
                any(), any(), any())).thenReturn(Flux.empty());
        when(movementRepository.save(any(AccountMovement.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(ledger.apply(account, new BigDecimal("100"), MovementType.DEPOSIT, null))
                .expectNextMatches(saved -> saved.getBalance().compareTo(new BigDecimal("97.50")) == 0)
                .verifyComplete();
    }

    private Account.AccountBuilder baseAccount() {
        return Account.builder()
                .id("a1")
                .customerId("c1")
                .accountType(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .freeMonthlyTransactions(5)
                .transactionCommissionFee(new BigDecimal("2.50"));
    }
}
