package com.bank.account.repository;

import com.bank.account.model.AccountMovement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Custom Spring Data operations that require a single Criteria on the same
 * field (e.g. {@code occurredAt} with $gte + $lt), which Spring Data derived
 * queries cannot express in a single BSON document.
 */
public interface AccountMovementRepositoryCustom {

    /**
     * Lists movements whose {@code occurredAt} falls in the range [from, to).
     */
    Flux<AccountMovement> findInRange(Instant from, Instant to);

    /**
     * Lists movements of a given account in the range [from, to).
     */
    Flux<AccountMovement> findByAccountIdInRange(String accountId, Instant from, Instant to);

    /**
     * Counts movements of a given account in the range [from, to).
     */
    Mono<Long> countByAccountIdInRange(String accountId, Instant from, Instant to);
}
