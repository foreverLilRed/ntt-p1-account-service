package com.bank.account.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Bank account (passive product) owned by one or more customers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "accounts")
public class Account {

    @Id
    private String id;

    /** Primary customer that opened the account. */
    private String customerId;

    private AccountType accountType;

    private BigDecimal balance;

    private BigDecimal maintenanceFee;

    /** Maximum monthly movements; null means unlimited. */
    private Integer monthlyMovementLimit;

    /** Day of month allowed for FIXED_TERM movements (1-31). */
    private Integer allowedTransactionDay;

    @Builder.Default
    private List<String> holders = new ArrayList<>();

    @Builder.Default
    private List<String> authorizedSigners = new ArrayList<>();

    private AccountStatus status;

    private Instant createdAt;

    private Instant updatedAt;
}
