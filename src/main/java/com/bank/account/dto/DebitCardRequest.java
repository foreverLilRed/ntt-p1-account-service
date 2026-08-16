package com.bank.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Payload to create or update a debit card.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitCardRequest {

    @NotBlank
    private String customerId;

    @NotBlank
    private String cardNumber;

    @NotBlank
    private String primaryAccountId;

    @NotEmpty
    private List<String> accountIds;
}
