package com.bank.account.mapper;

import com.bank.account.dto.DebitCardRequest;
import com.bank.account.dto.DebitCardResponse;
import com.bank.account.model.DebitCard;
import com.bank.account.model.DebitCardStatus;

import java.time.Instant;
import java.util.ArrayList;

/**
 * Maps debit card entities and DTOs.
 */
public final class DebitCardMapper {

    private DebitCardMapper() {
    }

    /**
     * Builds a new debit card.
     *
     * @param request payload
     * @return entity
     */
    public static DebitCard toEntity(DebitCardRequest request) {
        Instant now = Instant.now();
        return DebitCard.builder()
                .customerId(request.getCustomerId())
                .cardNumber(request.getCardNumber())
                .primaryAccountId(request.getPrimaryAccountId())
                .accountIds(new ArrayList<>(request.getAccountIds()))
                .status(DebitCardStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Maps an entity to the API response.
     *
     * @param card entity
     * @return response
     */
    public static DebitCardResponse toResponse(DebitCard card) {
        return DebitCardResponse.builder()
                .id(card.getId())
                .customerId(card.getCustomerId())
                .cardNumber(card.getCardNumber())
                .primaryAccountId(card.getPrimaryAccountId())
                .accountIds(card.getAccountIds())
                .status(card.getStatus())
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .build();
    }
}
