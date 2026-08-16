package com.bank.account.repository;

import com.bank.account.model.DebitCard;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive repository for debit cards.
 */
@Repository
public interface DebitCardRepository extends ReactiveMongoRepository<DebitCard, String> {

    /**
     * Lists debit cards of a customer.
     *
     * @param customerId customer
     * @return cards
     */
    Flux<DebitCard> findByCustomerId(String customerId);

    /**
     * Finds a card by its printed number.
     *
     * @param cardNumber PAN
     * @return card
     */
    Mono<DebitCard> findByCardNumber(String cardNumber);
}
