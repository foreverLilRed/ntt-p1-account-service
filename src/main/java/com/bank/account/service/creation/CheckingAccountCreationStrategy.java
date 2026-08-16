package com.bank.account.service.creation;

import com.bank.account.config.AccountProperties;
import com.bank.account.dto.AccountRequest;
import com.bank.account.mapper.AccountMapper;
import com.bank.account.model.Account;
import com.bank.account.model.AccountProductVariant;
import com.bank.account.model.AccountType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Creation strategy for standard CHECKING accounts.
 */
@Component
@RequiredArgsConstructor
public class CheckingAccountCreationStrategy implements AccountCreationStrategy {

    private final AccountProperties accountProperties;

    @Override
    public AccountType supportedType() {
        return AccountType.CHECKING;
    }

    @Override
    public boolean supports(AccountType type, String profile) {
        return type == AccountType.CHECKING && !"PYME".equalsIgnoreCase(profile);
    }

    @Override
    public Mono<Void> validateProductRules(AccountRequest request) {
        return Mono.empty();
    }

    @Override
    public Account buildEntity(AccountRequest request) {
        return AccountMapper.toEntity(request, accountProperties,
                accountProperties.getChecking().getMaintenanceFee(),
                AccountProductVariant.STANDARD, null);
    }
}
