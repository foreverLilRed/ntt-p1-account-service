package com.bank.account.dto;

import com.bank.account.model.TransferType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Result of a completed transfer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {

    private String transferId;
    private String sourceAccountId;
    private String destinationAccountId;
    private BigDecimal amount;
    private TransferType transferType;
    private Instant occurredAt;
}
