package com.bank.account.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient beans used for inter-service communication.
 */
@Configuration
public class WebClientConfig {

    /**
     * Builds a WebClient pointed at customer-service.
     *
     * @param properties customer service properties
     * @return configured WebClient
     */
    @Bean
    public WebClient customerWebClient(CustomerServiceProperties properties) {
        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
