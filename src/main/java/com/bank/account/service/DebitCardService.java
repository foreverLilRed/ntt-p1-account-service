package com.bank.account.service;

import com.bank.account.dto.DebitCardRequest;
import com.bank.account.dto.DebitCardResponse;
import com.bank.account.dto.TransactionRequest;
import com.bank.account.exception.BusinessException;
import com.bank.account.mapper.DebitCardMapper;
import com.bank.account.model.Account;
import com.bank.account.model.DebitCard;
import com.bank.account.model.DebitCardStatus;
import com.bank.account.model.MovementType;
import com.bank.account.repository.AccountRepository;
import com.bank.account.repository.DebitCardRepository;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Debit card CRUD and POS-style payments against linked accounts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DebitCardService {

    private final DebitCardRepository debitCardRepository;
    private final AccountRepository accountRepository;
    private final AccountLedger accountLedger;
    private final DebtStatusStore debtStatusStore;

    /**
     * Issues a debit card linked to customer accounts.
     *
     * @param request payload
     * @return created card
     */
    public Single<DebitCardResponse> create(DebitCardRequest request) {
        return RxJava3Adapter.monoToSingle(
                debtStatusStore.hasOverdueDebt(request.getCustomerId())
                        .flatMap(overdue -> Boolean.TRUE.equals(overdue)
                                ? Mono.error(new BusinessException(
                                "Customer has overdue credit debt and cannot acquire a new product",
                                HttpStatus.BAD_REQUEST))
                                : Mono.empty())
                        .then(Mono.defer(() -> validateAccounts(request)))
                        .then(Mono.defer(() -> debitCardRepository.findByCardNumber(request.getCardNumber())
                                .flatMap(existing -> Mono.<DebitCard>error(new BusinessException(
                                        "Debit card number already registered", HttpStatus.CONFLICT)))
                                .switchIfEmpty(Mono.defer(() ->
                                        debitCardRepository.save(DebitCardMapper.toEntity(request))))))
                        .map(DebitCardMapper::toResponse)
        );
    }

    /**
     * Lists all debit cards.
     *
     * @return cards
     */
    public Observable<DebitCardResponse> findAll() {
        return RxJava3Adapter.fluxToObservable(
                debitCardRepository.findAll().map(DebitCardMapper::toResponse)
        );
    }

    /**
     * Finds a debit card by id.
     *
     * @param id card id
     * @return card
     */
    public Single<DebitCardResponse> findById(String id) {
        return RxJava3Adapter.monoToSingle(findCard(id).map(DebitCardMapper::toResponse));
    }

    /**
     * Pays with the debit card, debiting the primary account first then others.
     *
     * @param id      card id
     * @param request amount
     * @return updated card
     */
    public Single<DebitCardResponse> pay(String id, TransactionRequest request) {
        return RxJava3Adapter.monoToSingle(
                findCard(id).flatMap(card -> {
                    if (card.getStatus() != DebitCardStatus.ACTIVE) {
                        return Mono.error(new BusinessException("Debit card is not active", HttpStatus.BAD_REQUEST));
                    }
                    return debitInOrder(card, request.getAmount()).thenReturn(card);
                }).map(DebitCardMapper::toResponse)
        );
    }

    /**
     * Resolves a card by printed number for Yanki association.
     *
     * @param cardNumber PAN
     * @return card
     */
    public Mono<DebitCard> findByCardNumber(String cardNumber) {
        return debitCardRepository.findByCardNumber(cardNumber)
                .switchIfEmpty(Mono.error(new BusinessException("Debit card not found", HttpStatus.NOT_FOUND)));
    }

    /**
     * Debits the primary account of a debit card (Yanki top-up/withdraw).
     *
     * @param cardNumber card PAN
     * @param amount     amount
     * @param type       movement type
     * @return primary account after posting
     */
    public Mono<Account> applyOnPrimaryAccount(String cardNumber, BigDecimal amount, MovementType type) {
        return findByCardNumber(cardNumber)
                .flatMap(card -> {
                    if (card.getStatus() != DebitCardStatus.ACTIVE) {
                        return Mono.error(new BusinessException("Debit card is not active", HttpStatus.BAD_REQUEST));
                    }
                    return accountRepository.findById(card.getPrimaryAccountId())
                            .switchIfEmpty(Mono.error(new BusinessException("Account not found", HttpStatus.NOT_FOUND)))
                            .flatMap(account -> accountLedger.apply(account, amount, type, null));
                });
    }

    private Mono<DebitCard> findCard(String id) {
        return debitCardRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException("Debit card not found", HttpStatus.NOT_FOUND)));
    }

    private Mono<Void> validateAccounts(DebitCardRequest request) {
        LinkedHashSet<String> ids = new LinkedHashSet<>(request.getAccountIds());
        ids.add(request.getPrimaryAccountId());
        return Flux.fromIterable(ids)
                .concatMap(accountId -> accountRepository.findById(accountId)
                        .switchIfEmpty(Mono.error(new BusinessException(
                                "Account not found: " + accountId, HttpStatus.NOT_FOUND)))
                        .flatMap(account -> {
                            if (!account.getCustomerId().equals(request.getCustomerId())) {
                                return Mono.error(new BusinessException(
                                        "Account does not belong to the customer", HttpStatus.BAD_REQUEST));
                            }
                            return Mono.just(account);
                        }))
                .then();
    }

    private Mono<Void> debitInOrder(DebitCard card, BigDecimal amount) {
        List<String> order = new ArrayList<>();
        order.add(card.getPrimaryAccountId());
        card.getAccountIds().stream()
                .filter(id -> !id.equals(card.getPrimaryAccountId()))
                .forEach(order::add);
        return debitRemaining(order, 0, amount);
    }

    private Mono<Void> debitRemaining(List<String> accountIds, int index, BigDecimal remaining) {
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.empty();
        }
        if (index >= accountIds.size()) {
            return Mono.error(new BusinessException("Insufficient funds across linked accounts",
                    HttpStatus.BAD_REQUEST));
        }
        return accountRepository.findById(accountIds.get(index))
                .switchIfEmpty(Mono.error(new BusinessException("Account not found", HttpStatus.NOT_FOUND)))
                .flatMap(account -> {
                    BigDecimal take = remaining.min(account.getBalance());
                    if (take.compareTo(BigDecimal.ZERO) <= 0) {
                        return debitRemaining(accountIds, index + 1, remaining);
                    }
                    return accountLedger.apply(account, take, MovementType.DEBIT_PAYMENT, null)
                            .then(debitRemaining(accountIds, index + 1, remaining.subtract(take)));
                });
    }
}
