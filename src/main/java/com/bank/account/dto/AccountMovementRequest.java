package com.bank.account.dto;

import com.bank.account.model.MovementType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request payload for CRUD operations on account movements.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountMovementRequest {

    @NotBlank
    private String accountId;

    @NotBlank
    private String customerId;

    @NotNull
    private MovementType movementType;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
}
