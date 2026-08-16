package com.bank.account.client;

import com.bank.account.dto.CustomerDto;
import com.bank.account.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * HTTP client that validates customers against customer-service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerClient {

    private final WebClient customerWebClient;
    private final ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory;

    /**
     * Retrieves a customer by id from customer-service.
     *
     * @param customerId customer identifier
     * @return customer projection
     */
    public Mono<CustomerDto> findById(String customerId) {
        log.debug("Calling customer-service for customerId={}", customerId);
        Mono<CustomerDto> call = customerWebClient.get()
                .uri("/api/v1/customers/{id}", customerId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new BusinessException("Customer not found", HttpStatus.NOT_FOUND)))
                .bodyToMono(CustomerDto.class);
        return circuitBreakerFactory.create("customerService").run(call, throwable -> {
            if (throwable instanceof BusinessException businessException) {
                return Mono.error(businessException);
            }
            log.error("customer-service circuit open or timeout", throwable);
            return Mono.error(new BusinessException(
                    "Customer service unavailable", HttpStatus.SERVICE_UNAVAILABLE));
        });
    }
}
