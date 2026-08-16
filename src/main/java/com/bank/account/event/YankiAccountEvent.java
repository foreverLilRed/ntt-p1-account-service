package com.bank.account.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Yanki request/result events exchanged with account-service over Kafka.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YankiAccountEvent {

    private String correlationId;
    private String walletId;
    private String cardNumber;
    private BigDecimal amount;
    private boolean success;
    private String message;
    private String primaryAccountId;
}
