package com.bank.account.service.creation;

import com.bank.account.dto.AccountRequest;
import com.bank.account.dto.CustomerDto;
import reactor.core.publisher.Mono;

/**
 * Single step in the account-creation eligibility chain.
 */
public interface AccountCreationRule {

    /**
     * Validates a creation request.
     *
     * @param customer customer projection
     * @param request  create payload
     * @return completion or error
     */
    Mono<Void> validate(CustomerDto customer, AccountRequest request);
}
