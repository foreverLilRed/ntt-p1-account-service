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

import java.math.BigDecimal;

/**
 * PYME checking account: no maintenance fee and credit-card requirement (RP2-003).
 */
@Component
@RequiredArgsConstructor
public class PymeCheckingAccountCreationStrategy implements AccountCreationStrategy {

    private final AccountProperties accountProperties;

    @Override
    public AccountType supportedType() {
        return AccountType.CHECKING;
    }

    @Override
    public boolean supports(AccountType type, String profile) {
        return type == AccountType.CHECKING && "PYME".equalsIgnoreCase(profile);
    }

    @Override
    public Mono<Void> validateProductRules(AccountRequest request) {
        return Mono.empty();
    }

    @Override
    public Account buildEntity(AccountRequest request) {
        BigDecimal fee = accountProperties.getPyme().getMaintenanceFee() == null
                ? BigDecimal.ZERO
                : accountProperties.getPyme().getMaintenanceFee();
        return AccountMapper.toEntity(request, accountProperties, fee,
                AccountProductVariant.PYME, null);
    }
}
