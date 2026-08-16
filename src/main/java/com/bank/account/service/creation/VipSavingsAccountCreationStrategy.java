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
 * VIP savings account: minimum daily average and credit-card requirement (RP2-001).
 */
@Component
@RequiredArgsConstructor
public class VipSavingsAccountCreationStrategy implements AccountCreationStrategy {

    private final AccountProperties accountProperties;

    @Override
    public AccountType supportedType() {
        return AccountType.SAVINGS;
    }

    @Override
    public boolean supports(AccountType type, String profile) {
        return type == AccountType.SAVINGS && "VIP".equalsIgnoreCase(profile);
    }

    @Override
    public Mono<Void> validateProductRules(AccountRequest request) {
        return Mono.empty();
    }

    @Override
    public Account buildEntity(AccountRequest request) {
        return AccountMapper.toEntity(request, accountProperties, BigDecimal.ZERO,
                AccountProductVariant.VIP, accountProperties.getVip().getMinDailyAverage());
    }
}
