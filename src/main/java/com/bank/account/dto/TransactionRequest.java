package com.bank.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request payload for deposit or withdrawal operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotNull
    @DecimalMin(value = "0.01", message = "amount must be greater than zero")
    private BigDecimal amount;
}
