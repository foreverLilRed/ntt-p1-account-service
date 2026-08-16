package com.bank.account.dto;

import com.bank.account.model.MovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response representation of an account movement.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountMovementResponse {

    private String id;
    private String accountId;
    private String customerId;
    private MovementType movementType;
    private BigDecimal amount;
    private BigDecimal commissionAmount;
    private String transferId;
    private BigDecimal balanceAfter;
    private Instant occurredAt;
}
