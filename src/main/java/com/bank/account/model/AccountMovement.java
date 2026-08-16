package com.bank.account.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Movement performed against a bank account.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "account_movements")
public class AccountMovement {

    @Id
    private String id;

    private String accountId;

    private String customerId;

    private MovementType movementType;

    private BigDecimal amount;

    @Builder.Default
    private BigDecimal commissionAmount = BigDecimal.ZERO;

    private String transferId;

    private BigDecimal balanceAfter;

    private Instant occurredAt;
}
