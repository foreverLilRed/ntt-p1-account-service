package com.bank.account.repository;

import com.bank.account.model.Account;
import com.bank.account.model.AccountType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive repository for account documents.
 * Uses derived query methods only (no {@code @Query}).
 */
@Repository
public interface AccountRepository extends ReactiveMongoRepository<Account, String> {

    /**
     * Lists accounts belonging to a customer.
     *
     * @param customerId customer identifier
     * @return account stream
     */
    Flux<Account> findByCustomerId(String customerId);

    /**
     * Counts accounts of a given type for a customer.
     *
     * @param customerId  customer identifier
     * @param accountType account type
     * @return count
     */
    Mono<Long> countByCustomerIdAndAccountType(String customerId, AccountType accountType);
}
