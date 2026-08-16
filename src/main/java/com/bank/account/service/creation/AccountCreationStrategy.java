package com.bank.account.service.creation;

import com.bank.account.dto.AccountRequest;
import com.bank.account.model.Account;
import com.bank.account.model.AccountType;
import reactor.core.publisher.Mono;

/**
 * Strategy that encapsulates product-specific rules for creating an account
 * of a concrete {@link AccountType}.
 */
public interface AccountCreationStrategy {

    /**
     * Account type handled by this strategy.
     *
     * @return supported account type
     */
    AccountType supportedType();

    /**
     * Validates product-specific creation rules for the requested account.
     *
     * @param request create payload
     * @return completion signal, or error when a product rule is violated
     */
    Mono<Void> validateProductRules(AccountRequest request);

    /**
     * Builds the account entity applying product defaults (fees, limits, etc.).
     *
     * @param request create payload
     * @return new account entity ready to persist
     */
    Account buildEntity(AccountRequest request);
}
