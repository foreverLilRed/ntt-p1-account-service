package com.bank.account.repository;

import com.bank.account.model.AccountMovement;
import org.springframework.data.mongodb.repository.Query;
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
     * Lists movements of an account within a time window.
     *
     * @param accountId account identifier
     * @param from      inclusive start
     * @param to        exclusive end
     * @return movement stream
     */
    Flux<AccountMovement> findByAccountIdAndOccurredAtGreaterThanEqualAndOccurredAtLessThan(
            String accountId, Instant from, Instant to);

    /**
     * Lists all movements within a time window.
     *
     * @param from inclusive start
     * @param to   exclusive end
     * @return movement stream
     */
    Flux<AccountMovement> findByOccurredAtGreaterThanEqualAndOccurredAtLessThan(Instant from, Instant to);

    /**
     * Counts movements of an account within a time window.
     *
     * @param accountId account identifier
     * @param from      inclusive start
     * @param to        exclusive end
     * @return count
     */
    @Query(value = "{ 'accountId': ?0, 'occurredAt': { $gte: ?1, $lt: ?2 } }", count = true)
    Mono<Long> countByAccountIdAndOccurredAtGreaterThanEqualAndOccurredAtLessThan(
            String accountId, Instant from, Instant to);
}
