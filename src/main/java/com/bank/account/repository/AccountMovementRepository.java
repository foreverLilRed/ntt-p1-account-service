package com.bank.account.repository;

import com.bank.account.model.AccountMovement;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Reactive repository for account movement documents.
 */
@Repository
public interface AccountMovementRepository
        extends ReactiveMongoRepository<AccountMovement, String>, AccountMovementRepositoryCustom {

    /**
     * Lists movements of an account ordered by occurrence descending.
     *
     * @param accountId account identifier
     * @return movement stream
     */
    Flux<AccountMovement> findByAccountIdOrderByOccurredAtDesc(String accountId);
}
