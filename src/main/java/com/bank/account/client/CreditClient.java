package com.bank.account.client;

import com.bank.account.dto.ActiveCardResponse;
import com.bank.account.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * HTTP client used to verify credit cards owned by a customer.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreditClient {

    private final WebClient creditWebClient;
    private final ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory;

    /**
     * Returns whether the customer currently has an active credit card.
     *
     * @param customerId customer identifier
     * @return true when at least one ACTIVE card exists
     */
    public Mono<Boolean> hasActiveCreditCard(String customerId) {
        Mono<Boolean> call = creditWebClient.get()
                .uri("/api/v1/credit-cards/customer/{customerId}/active", customerId)
                .retrieve()
                .bodyToMono(ActiveCardResponse.class)
                .map(ActiveCardResponse::isHasActiveCreditCard)
                .defaultIfEmpty(false);
        return circuitBreakerFactory.create("creditService").run(call, throwable -> {
            log.error("credit-service circuit open or timeout", throwable);
            return Mono.error(new BusinessException(
                    "Credit service unavailable", HttpStatus.SERVICE_UNAVAILABLE));
        });
    }
}
