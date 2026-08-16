package com.bank.account.service;

import com.bank.account.client.CustomerClient;
import com.bank.account.dto.AccountRequest;
import com.bank.account.dto.AccountResponse;
import com.bank.account.dto.BalanceResponse;
import com.bank.account.dto.TransactionRequest;
import com.bank.account.exception.BusinessException;
import com.bank.account.mapper.AccountMapper;
import com.bank.account.model.Account;
import com.bank.account.model.AccountMovement;
import com.bank.account.model.AccountStatus;
import com.bank.account.model.AccountType;
import com.bank.account.model.MovementType;
import com.bank.account.repository.AccountMovementRepository;
import com.bank.account.repository.AccountRepository;
import com.bank.account.service.creation.AccountCreationStrategy;
import com.bank.account.service.creation.AccountCreationStrategyFactory;
import com.bank.account.service.creation.CustomerEligibilityValidator;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;

/**
 * Application service for account CRUD and transactional operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMovementRepository movementRepository;
    private final CustomerClient customerClient;
    private final AccountCreationStrategyFactory creationStrategyFactory;
    private final CustomerEligibilityValidator customerEligibilityValidator;

    /**
     * Creates an account applying product and customer-type rules.
     *
     * @param request create payload
     * @return created account
     */
    public Single<AccountResponse> create(AccountRequest request) {
        log.info("Creating account type={} for customerId={}", request.getAccountType(), request.getCustomerId());
        return RxJava3Adapter.monoToSingle(
                customerClient.findById(request.getCustomerId())
                        .flatMap(customer -> {
                            AccountCreationStrategy strategy =
                                    creationStrategyFactory.resolve(request.getAccountType());
                            return customerEligibilityValidator.validate(customer, request)
                                    .then(strategy.validateProductRules(request))
                                    .then(Mono.defer(() ->
                                            accountRepository.save(strategy.buildEntity(request))));
                        })
                        .map(AccountMapper::toResponse)
        );
    }

    /**
     * Lists all accounts.
     *
     * @return account stream
     */
    public Observable<AccountResponse> findAll() {
        log.debug("Listing all accounts");
        return RxJava3Adapter.fluxToObservable(
                accountRepository.findAll().map(AccountMapper::toResponse)
        );
    }

    /**
     * Finds an account by id.
     *
     * @param id account id
     * @return account
     */
    public Single<AccountResponse> findById(String id) {
        return RxJava3Adapter.monoToSingle(findAccount(id).map(AccountMapper::toResponse));
    }

    /**
     * Updates mutable account fields.
     *
     * @param id      account id
     * @param request update payload
     * @return updated account
     */
    public Single<AccountResponse> update(String id, AccountRequest request) {
        log.info("Updating account id={}", id);
        return RxJava3Adapter.monoToSingle(
                findAccount(id)
                        .flatMap(account -> customerClient.findById(account.getCustomerId())
                                .flatMap(customer -> {
                                    if (request.getHolders() != null && request.getHolders().isEmpty()) {
                                        return Mono.error(new BusinessException(
                                                "Business accounts require at least one holder",
                                                HttpStatus.BAD_REQUEST));
                                    }
                                    return customerEligibilityValidator
                                            .validateHoldersAndSigners(customer.getCustomerType(), request)
                                            .then(Mono.defer(() -> {
                                                Account updated = AccountMapper.applyUpdate(account, request);
                                                return accountRepository.save(updated);
                                            }));
                                }))
                        .map(AccountMapper::toResponse)
        );
    }

    /**
     * Deletes an account by id.
     *
     * @param id account id
     * @return completion signal
     */
    public Completable delete(String id) {
        log.info("Deleting account id={}", id);
        return RxJava3Adapter.monoToCompletable(
                findAccount(id).flatMap(account -> accountRepository.deleteById(id)).then()
        );
    }

    /**
     * Returns the available balance of an account.
     *
     * @param id account id
     * @return balance snapshot
     */
    public Single<BalanceResponse> getBalance(String id) {
        return RxJava3Adapter.monoToSingle(findAccount(id).map(AccountMapper::toBalance));
    }

    /**
     * Performs a deposit on an account.
     *
     * @param id      account id
     * @param request transaction payload
     * @return updated account
     */
    public Single<AccountResponse> deposit(String id, TransactionRequest request) {
        return RxJava3Adapter.monoToSingle(applyTransaction(id, request.getAmount(), MovementType.DEPOSIT)
                .map(AccountMapper::toResponse));
    }

    /**
     * Performs a withdrawal on an account.
     *
     * @param id      account id
     * @param request transaction payload
     * @return updated account
     */
    public Single<AccountResponse> withdraw(String id, TransactionRequest request) {
        return RxJava3Adapter.monoToSingle(applyTransaction(id, request.getAmount(), MovementType.WITHDRAWAL)
                .map(AccountMapper::toResponse));
    }

    private Mono<Account> findAccount(String id) {
        return accountRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException("Account not found", HttpStatus.NOT_FOUND)));
    }

    private Mono<Account> applyTransaction(String accountId, BigDecimal amount, MovementType movementType) {
        log.info("Applying {} of {} on accountId={}", movementType, amount, accountId);
        return findAccount(accountId)
                .flatMap(account -> {
                    if (account.getStatus() != AccountStatus.ACTIVE) {
                        return Mono.error(new BusinessException("Account is not active", HttpStatus.BAD_REQUEST));
                    }
                    return validateMovementRules(account)
                            .then(Mono.defer(() -> {
                                BigDecimal newBalance = movementType == MovementType.DEPOSIT
                                        ? account.getBalance().add(amount)
                                        : account.getBalance().subtract(amount);
                                if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                                    return Mono.error(new BusinessException(
                                            "Insufficient funds", HttpStatus.BAD_REQUEST));
                                }
                                account.setBalance(newBalance);
                                account.setUpdatedAt(Instant.now());
                                AccountMovement movement = AccountMovement.builder()
                                        .accountId(account.getId())
                                        .customerId(account.getCustomerId())
                                        .movementType(movementType)
                                        .amount(amount)
                                        .balanceAfter(newBalance)
                                        .occurredAt(Instant.now())
                                        .build();
                                return movementRepository.save(movement)
                                        .then(accountRepository.save(account));
                            }));
                });
    }

    private Mono<Void> validateMovementRules(Account account) {
        // #region agent log
        debugLog("A", "AccountService.java:validateMovementRules:entry", "validateMovementRules entry",
                "{\"accountId\":\"" + account.getId() + "\",\"accountType\":\"" + account.getAccountType()
                        + "\",\"monthlyMovementLimit\":" + account.getMonthlyMovementLimit()
                        + ",\"allowedTransactionDay\":" + account.getAllowedTransactionDay() + "}");
        // #endregion
        if (account.getAccountType() == AccountType.FIXED_TERM) {
            int today = LocalDate.now(ZoneOffset.UTC).getDayOfMonth();
            if (account.getAllowedTransactionDay() == null || today != account.getAllowedTransactionDay()) {
                return Mono.error(new BusinessException(
                        "FIXED_TERM accounts only allow movements on day "
                                + account.getAllowedTransactionDay() + " of the month",
                        HttpStatus.BAD_REQUEST));
            }
            LocalDate start = LocalDate.now(ZoneOffset.UTC).with(TemporalAdjusters.firstDayOfMonth());
            LocalDate end = start.plusMonths(1);
            Instant from = start.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant to = end.atStartOfDay().toInstant(ZoneOffset.UTC);
            // #region agent log
            debugLog("A,C", "AccountService.java:validateMovementRules:fixedTerm",
                    "about to call derived count (FIXED_TERM)",
                    "{\"from\":\"" + from + "\",\"to\":\"" + to + "\"}");
            // #endregion
            return movementRepository.countByAccountIdInRange(
                            account.getId(), from, to)
                    .flatMap(count -> {
                        if (count >= 1) {
                            return Mono.error(new BusinessException(
                                    "FIXED_TERM accounts allow only one movement per month",
                                    HttpStatus.BAD_REQUEST));
                        }
                        return Mono.empty();
                    });
        }

        if (account.getAccountType() == AccountType.SAVINGS
                && account.getMonthlyMovementLimit() != null) {
            LocalDate start = LocalDate.now(ZoneOffset.UTC).with(TemporalAdjusters.firstDayOfMonth());
            LocalDate end = start.plusMonths(1);
            Instant from = start.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant to = end.atStartOfDay().toInstant(ZoneOffset.UTC);
            // #region agent log
            debugLog("A,B", "AccountService.java:validateMovementRules:savings",
                    "about to call derived count (SAVINGS)",
                    "{\"from\":\"" + from + "\",\"to\":\"" + to + "\"}");
            // #endregion
            return movementRepository.countByAccountIdInRange(
                            account.getId(), from, to)
                    .flatMap(count -> {
                        if (count >= account.getMonthlyMovementLimit()) {
                            return Mono.error(new BusinessException(
                                    "Monthly movement limit reached for SAVINGS account",
                                    HttpStatus.BAD_REQUEST));
                        }
                        return Mono.empty();
                    });
        }

        return Mono.empty();
    }

    // #region agent log
    private void debugLog(String hypothesisId, String location, String message, String data) {
        try {
            String line = "{\"sessionId\":\"e7617b\",\"hypothesisId\":\"" + hypothesisId
                    + "\",\"location\":\"" + location + "\",\"message\":\"" + message
                    + "\",\"data\":" + data + ",\"timestamp\":" + System.currentTimeMillis() + "}\n";
            java.nio.file.Files.write(
                    java.nio.file.Paths.get("C:\\Proyectos\\Proyecto General\\Parte 01\\debug-e7617b.log"),
                    line.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception ignored) {
        }
    }
    // #endregion
}
