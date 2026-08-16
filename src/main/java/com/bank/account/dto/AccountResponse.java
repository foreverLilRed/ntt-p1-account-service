package com.bank.account.dto;

import com.bank.account.model.AccountStatus;
import com.bank.account.model.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Response representation of an account.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private String id;
    private String customerId;
    private AccountType accountType;
    private BigDecimal balance;
    private BigDecimal maintenanceFee;
    private Integer monthlyMovementLimit;
    private Integer allowedTransactionDay;
    private List<String> holders;
    private List<String> authorizedSigners;
    private AccountStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
