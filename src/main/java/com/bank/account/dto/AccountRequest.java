package com.bank.account.dto;

import com.bank.account.model.AccountStatus;
import com.bank.account.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request payload to create or update an account.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

    @NotBlank
    private String customerId;

    @NotNull
    private AccountType accountType;

    /** Required for FIXED_TERM accounts (day of month 1-31). */
    private Integer allowedTransactionDay;

    private List<String> holders;

    private List<String> authorizedSigners;

    private AccountStatus status;
}
