package com.bank.account.dto;

import com.bank.account.model.DebitCardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Debit card API representation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitCardResponse {

    private String id;
    private String customerId;
    private String cardNumber;
    private String primaryAccountId;
    private List<String> accountIds;
    private DebitCardStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
