package com.bank.account.service.creation;

import com.bank.account.exception.BusinessException;
import com.bank.account.model.AccountType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Resolves the {@link AccountCreationStrategy} matching type and customer profile.
 */
@Component
public class AccountCreationStrategyFactory {

    private final List<AccountCreationStrategy> strategies;

    /**
     * Builds the factory registry from all available strategies.
     *
     * @param strategies strategies discovered by Spring
     */
    public AccountCreationStrategyFactory(List<AccountCreationStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * Resolves the most specific strategy for the requested account.
     *
     * @param type    requested account type
     * @param profile customer profile
     * @return matching strategy
     */
    public AccountCreationStrategy resolve(AccountType type, String profile) {
        return strategies.stream()
                .sorted(Comparator.comparingInt(this::specificity).reversed())
                .filter(strategy -> strategy.supports(type, profile))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "Unsupported account type: " + type, HttpStatus.BAD_REQUEST));
    }

    private int specificity(AccountCreationStrategy strategy) {
        if (strategy instanceof VipSavingsAccountCreationStrategy
                || strategy instanceof PymeCheckingAccountCreationStrategy) {
            return 2;
        }
        return 1;
    }
}
