package com.bank.account.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI documentation configuration for the account service.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Builds the OpenAPI descriptor.
     *
     * @return OpenAPI bean
     */
    @Bean
    public OpenAPI accountOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Account Service API")
                        .description("Passive products: savings, checking and fixed-term accounts")
                        .version("1.0.0"));
    }
}
