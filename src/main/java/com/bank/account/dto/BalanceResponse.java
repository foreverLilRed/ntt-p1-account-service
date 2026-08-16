package com.bank.account.dto;

import com.bank.account.model.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Available balance snapshot for an account.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {

    private String accountId;
    private String customerId;
    private AccountType accountType;
    private BigDecimal availableBalance;
}
