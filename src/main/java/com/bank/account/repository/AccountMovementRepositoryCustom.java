package com.bank.account.repository;

import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Custom Spring Data operations that require a single Criteria on the same
 * field (e.g. {@code occurredAt} with $gte + $lt), which Spring Data derived
 * queries cannot express in a single BSON document.
 */
public interface AccountMovementRepositoryCustom {

    /**
     * Counts movements of a given account whose {@code occurredAt} falls in
     * the range [from, to).
     */
    Mono<Long> countByAccountIdInRange(String accountId, Instant from, Instant to);
}
