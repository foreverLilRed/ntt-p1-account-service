package com.bank.account.service;

import com.bank.account.dto.AccountMovementRequest;
import com.bank.account.dto.AccountMovementResponse;
import com.bank.account.exception.BusinessException;
import com.bank.account.mapper.AccountMapper;
import com.bank.account.model.AccountMovement;
import com.bank.account.repository.AccountMovementRepository;
import com.bank.account.repository.AccountRepository;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * CRUD service for account movement documents.
 * Prefer deposit/withdraw endpoints for balance-affecting operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountMovementService {

    private final AccountMovementRepository movementRepository;
    private final AccountRepository accountRepository;

    /**
     * Creates a movement record (administrative CRUD).
     *
     * @param request create payload
     * @return created movement
     */
    public Single<AccountMovementResponse> create(AccountMovementRequest request) {
        log.info("Creating movement record for accountId={}", request.getAccountId());
        return RxJava3Adapter.monoToSingle(
                accountRepository.findById(request.getAccountId())
                        .switchIfEmpty(Mono.error(new BusinessException("Account not found", HttpStatus.NOT_FOUND)))
                        .flatMap(account -> {
                            AccountMovement movement = AccountMapper.toMovementEntity(request);
                            movement.setBalanceAfter(account.getBalance());
                            return movementRepository.save(movement);
                        })
                        .map(AccountMapper::toMovementResponse)
        );
    }

    /**
     * Lists all movements.
     *
     * @return movement stream
     */
    public Observable<AccountMovementResponse> findAll() {
        return RxJava3Adapter.fluxToObservable(
                movementRepository.findAll().map(AccountMapper::toMovementResponse)
        );
    }

    /**
     * Lists movements of a specific account.
     *
     * @param accountId account identifier
     * @return movement stream
     */
    public Observable<AccountMovementResponse> findByAccountId(String accountId) {
        log.debug("Listing movements for accountId={}", accountId);
        return RxJava3Adapter.fluxToObservable(
                movementRepository.findByAccountIdOrderByOccurredAtDesc(accountId)
                        .map(AccountMapper::toMovementResponse)
        );
    }

    /**
     * Finds a movement by id.
     *
     * @param id movement id
     * @return movement
     */
    public Single<AccountMovementResponse> findById(String id) {
        return RxJava3Adapter.monoToSingle(
                movementRepository.findById(id)
                        .switchIfEmpty(Mono.error(new BusinessException("Movement not found", HttpStatus.NOT_FOUND)))
                        .map(AccountMapper::toMovementResponse)
        );
    }

    /**
     * Updates a movement record (administrative CRUD).
     *
     * @param id      movement id
     * @param request update payload
     * @return updated movement
     */
    public Single<AccountMovementResponse> update(String id, AccountMovementRequest request) {
        return RxJava3Adapter.monoToSingle(
                movementRepository.findById(id)
                        .switchIfEmpty(Mono.error(new BusinessException("Movement not found", HttpStatus.NOT_FOUND)))
                        .flatMap(existing -> {
                            existing.setAccountId(request.getAccountId());
                            existing.setCustomerId(request.getCustomerId());
                            existing.setMovementType(request.getMovementType());
                            existing.setAmount(request.getAmount());
                            existing.setOccurredAt(Instant.now());
                            return movementRepository.save(existing);
                        })
                        .map(AccountMapper::toMovementResponse)
        );
    }

    /**
     * Deletes a movement by id.
     *
     * @param id movement id
     * @return completion signal
     */
    public Completable delete(String id) {
        return RxJava3Adapter.monoToCompletable(
                movementRepository.findById(id)
                        .switchIfEmpty(Mono.error(new BusinessException("Movement not found", HttpStatus.NOT_FOUND)))
                        .flatMap(movement -> movementRepository.deleteById(id))
                        .then()
        );
    }
}
