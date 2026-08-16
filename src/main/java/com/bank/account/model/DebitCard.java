package com.bank.account.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Debit card linked to one or more bank accounts, with a primary account.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "debit_cards")
public class DebitCard {

    @Id
    private String id;

    private String customerId;

    private String cardNumber;

    private String primaryAccountId;

    @Builder.Default
    private List<String> accountIds = new ArrayList<>();

    private DebitCardStatus status;

    private Instant createdAt;

    private Instant updatedAt;
}
