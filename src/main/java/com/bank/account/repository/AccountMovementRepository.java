package com.bank.account.repository;

import com.bank.account.model.AccountMovement;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Reactive repository for account movement documents.
 */
@Repository
public interface AccountMovementRepository extends ReactiveMongoRepository<AccountMovement, String> {

    /**
     * Lists movements of an account ordered by occurrence descending.
     *
     * @param accountId account identifier
     * @return movement stream
     */
    Flux<AccountMovement> findByAccountIdOrderByOccurredAtDesc(String accountId);

    /**
     * Counts movements of an account within a time window.
     * Derived query: accountId == ?0 AND occurredAt >= ?1 AND occurredAt &lt; ?2.
     *
     * @param accountId account identifier
     * @param from      inclusive start
     * @param to        exclusive end
     * @return count
     */
    Mono<Long> countByAccountIdAndOccurredAtGreaterThanEqualAndOccurredAtLessThan(
            String accountId, Instant from, Instant to);
}
