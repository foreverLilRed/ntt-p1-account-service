package com.bank.account.service.creation;

import com.bank.account.config.AccountProperties;
import com.bank.account.dto.AccountRequest;
import com.bank.account.mapper.AccountMapper;
import com.bank.account.model.Account;
import com.bank.account.model.AccountType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Creation strategy for SAVINGS accounts.
 */
@Component
@RequiredArgsConstructor
public class SavingsAccountCreationStrategy implements AccountCreationStrategy {

    private final AccountProperties accountProperties;

    @Override
    public AccountType supportedType() {
        return AccountType.SAVINGS;
    }

    @Override
    public Mono<Void> validateProductRules(AccountRequest request) {
        return Mono.empty();
    }

    @Override
    public Account buildEntity(AccountRequest request) {
        Integer monthlyLimit = accountProperties.getSavings().getMonthlyMovementLimit();
        return AccountMapper.toEntity(request, BigDecimal.ZERO, monthlyLimit);
    }
}
