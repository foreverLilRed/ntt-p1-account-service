package com.bank.account.service.creation;

import com.bank.account.exception.BusinessException;
import com.bank.account.model.AccountType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the {@link AccountCreationStrategy} that matches a given
 * {@link AccountType}, indexing all Spring-managed strategies by type.
 */
@Component
public class AccountCreationStrategyFactory {

    private final Map<AccountType, AccountCreationStrategy> strategiesByType;

    /**
     * Builds the factory registry from all available strategies.
     *
     * @param strategies strategies discovered by Spring
     */
    public AccountCreationStrategyFactory(List<AccountCreationStrategy> strategies) {
        this.strategiesByType = new EnumMap<>(AccountType.class);
        for (AccountCreationStrategy strategy : strategies) {
            this.strategiesByType.put(strategy.supportedType(), strategy);
        }
    }

    /**
     * Resolves the strategy for the requested account type.
     *
     * @param type requested account type
     * @return matching strategy
     * @throws BusinessException when no strategy supports the type
     */
    public AccountCreationStrategy resolve(AccountType type) {
        AccountCreationStrategy strategy = strategiesByType.get(type);
        if (strategy == null) {
            throw new BusinessException("Unsupported account type: " + type, HttpStatus.BAD_REQUEST);
        }
        return strategy;
    }
}
