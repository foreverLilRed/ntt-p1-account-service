package com.bank.account.repository;

import com.bank.account.model.AccountMovement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Spring Data implementation using {@link ReactiveMongoTemplate}. Uses a
 * single {@link Criteria} per field so ranges over the same field ($gte + $lt)
 * are serialized into one BSON document.
 */
@RequiredArgsConstructor
public class AccountMovementRepositoryCustomImpl implements AccountMovementRepositoryCustom {

    private final ReactiveMongoTemplate template;

    @Override
    public Mono<Long> countByAccountIdInRange(String accountId, Instant from, Instant to) {
        Query query = Query.query(Criteria.where("accountId").is(accountId)
                .and("occurredAt").gte(from).lt(to));
        return template.count(query, AccountMovement.class);
    }
}
