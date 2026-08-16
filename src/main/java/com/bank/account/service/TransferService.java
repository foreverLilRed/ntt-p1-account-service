package com.bank.account.service;

import com.bank.account.dto.TransferRequest;
import com.bank.account.dto.TransferResponse;
import com.bank.account.exception.BusinessException;
import com.bank.account.model.Account;
import com.bank.account.model.MovementType;
import com.bank.account.model.TransferType;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Template-method style transfer orchestrator for OWN and THIRD_PARTY transfers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountService accountService;
    private final AccountLedger accountLedger;

    /**
     * Executes an intra-bank transfer.
     *
     * @param request transfer payload
     * @return transfer result
     */
    public Single<TransferResponse> transfer(TransferRequest request) {
        return RxJava3Adapter.monoToSingle(execute(request));
    }

    /**
     * Shared pipeline: validate, debit source, credit destination, compensate on failure.
     *
     * @param request transfer payload
     * @return transfer result
     */
    protected Mono<TransferResponse> execute(TransferRequest request) {
        if (request.getSourceAccountId().equals(request.getDestinationAccountId())) {
            return Mono.error(new BusinessException(
                    "Source and destination accounts must be different", HttpStatus.BAD_REQUEST));
        }
        String transferId = UUID.randomUUID().toString();
        Instant occurredAt = Instant.now();
        return Mono.zip(
                        accountService.findAccount(request.getSourceAccountId()),
                        accountService.findAccount(request.getDestinationAccountId()))
                .flatMap(tuple -> {
                    Account source = tuple.getT1();
                    Account destination = tuple.getT2();
                    return validateOwnership(source, destination, request.getTransferType())
                            .then(accountLedger.apply(source, request.getAmount(),
                                    MovementType.TRANSFER_OUT, transferId))
                            .flatMap(debited -> accountLedger.apply(destination, request.getAmount(),
                                            MovementType.TRANSFER_IN, transferId)
                                    .onErrorResume(error -> compensate(debited, request.getAmount(), transferId)
                                            .then(Mono.error(error))))
                            .thenReturn(TransferResponse.builder()
                                    .transferId(transferId)
                                    .sourceAccountId(source.getId())
                                    .destinationAccountId(destination.getId())
                                    .amount(request.getAmount())
                                    .transferType(request.getTransferType())
                                    .occurredAt(occurredAt)
                                    .build());
                });
    }

    private Mono<Void> validateOwnership(Account source, Account destination, TransferType type) {
        boolean sameCustomer = source.getCustomerId().equals(destination.getCustomerId());
        if (type == TransferType.OWN && !sameCustomer) {
            return Mono.error(new BusinessException(
                    "OWN transfers require both accounts to belong to the same customer",
                    HttpStatus.BAD_REQUEST));
        }
        if (type == TransferType.THIRD_PARTY && sameCustomer) {
            return Mono.error(new BusinessException(
                    "THIRD_PARTY transfers require accounts of different customers",
                    HttpStatus.BAD_REQUEST));
        }
        return Mono.empty();
    }

    private Mono<Account> compensate(Account debited, java.math.BigDecimal amount, String transferId) {
        log.warn("Compensating transfer {} on source {}", transferId, debited.getId());
        return accountService.findAccount(debited.getId())
                .flatMap(current -> accountLedger.apply(current, amount, MovementType.TRANSFER_IN, transferId));
    }
}
