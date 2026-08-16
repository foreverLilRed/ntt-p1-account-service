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
     * Whether this strategy applies to the requested type and customer profile.
     *
     * @param type    account type
     * @param profile customer profile
     * @return true when this strategy should be used
     */
    default boolean supports(AccountType type, String profile) {
        return supportedType() == type;
    }

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
