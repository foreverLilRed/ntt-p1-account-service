package com.bank.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Projection used to check if a customer has an active credit card.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveCardResponse {

    private boolean hasActiveCreditCard;
}
