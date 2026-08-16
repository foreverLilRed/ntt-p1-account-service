package com.bank.account.service.creation;

import com.bank.account.config.AccountProperties;
import com.bank.account.dto.AccountRequest;
import com.bank.account.dto.CustomerDto;
import com.bank.account.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * RP2-006: initial deposit must be at least the configured opening amount (can be zero).
 */
@Component
public class OpeningAmountRule implements AccountCreationRule {

    private final AccountProperties accountProperties;

    /**
     * @param accountProperties product configuration
     */
    public OpeningAmountRule(AccountProperties accountProperties) {
        this.accountProperties = accountProperties;
    }

    @Override
    public Mono<Void> validate(CustomerDto customer, AccountRequest request) {
        BigDecimal minimum = Optional.ofNullable(accountProperties.getOpening().getMinimumAmount())
                .orElse(BigDecimal.ZERO);
        BigDecimal deposit = Optional.ofNullable(request.getInitialDeposit()).orElse(BigDecimal.ZERO);
        if (deposit.compareTo(minimum) < 0) {
            return Mono.error(new BusinessException(
                    "Initial deposit must be at least " + minimum, HttpStatus.BAD_REQUEST));
        }
        return Mono.empty();
    }
}
