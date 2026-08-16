package com.bank.account.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Kafka payload for overdue-debt status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebtStatusChangedEvent {

    private String customerId;
    private boolean hasOverdueDebt;
    private Instant occurredAt;
}
