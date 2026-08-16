package com.bank.account.client;

import com.bank.account.dto.CustomerDto;
import com.bank.account.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * Retrieves a customer by id from customer-service.
     *
     * @param customerId customer identifier
     * @return customer projection
     */
    public Mono<CustomerDto> findById(String customerId) {
        log.debug("Calling customer-service for customerId={}", customerId);
        return customerWebClient.get()
                .uri("/api/v1/customers/{id}", customerId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new BusinessException("Customer not found", HttpStatus.NOT_FOUND)))
                .bodyToMono(CustomerDto.class);
    }
}
