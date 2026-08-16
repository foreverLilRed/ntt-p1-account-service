package com.bank.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the account microservice.
 */
@SpringBootApplication
public class AccountServiceApplication {

    /**
     * Boots the account service application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
