package com.bank.account.controller;

import com.bank.account.dto.DebitCardRequest;
import com.bank.account.dto.DebitCardResponse;
import com.bank.account.dto.TransactionRequest;
import com.bank.account.service.DebitCardService;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for debit cards.
 */
@RestController
@RequestMapping("/api/v1/debit-cards")
@RequiredArgsConstructor
@Tag(name = "Debit Cards", description = "Debit cards linked to bank accounts")
public class DebitCardController {

    private final DebitCardService debitCardService;

    /**
     * Issues a debit card.
     *
     * @param request payload
     * @return created card
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create debit card")
    public Single<DebitCardResponse> create(@Valid @RequestBody DebitCardRequest request) {
        return debitCardService.create(request);
    }

    /**
     * Lists debit cards.
     *
     * @return cards
     */
    @GetMapping
    @Operation(summary = "List debit cards")
    public Observable<DebitCardResponse> findAll() {
        return debitCardService.findAll();
    }

    /**
     * Gets a debit card.
     *
     * @param id card id
     * @return card
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get debit card by id")
    public Single<DebitCardResponse> findById(@PathVariable String id) {
        return debitCardService.findById(id);
    }

    /**
     * Pays with a debit card.
     *
     * @param id      card id
     * @param request amount
     * @return card
     */
    @PostMapping("/{id}/payments")
    @Operation(summary = "Pay with debit card")
    public Single<DebitCardResponse> pay(@PathVariable String id, @Valid @RequestBody TransactionRequest request) {
        return debitCardService.pay(id, request);
    }
}
