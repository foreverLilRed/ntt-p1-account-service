package com.bank.account.service;

import com.bank.account.client.CustomerClient;
import com.bank.account.dto.AccountRequest;
import com.bank.account.dto.AccountResponse;
import com.bank.account.dto.BalanceResponse;
import com.bank.account.dto.ProductMetricDto;
import com.bank.account.dto.TransactionRequest;
import com.bank.account.exception.BusinessException;
import com.bank.account.mapper.AccountMapper;
import com.bank.account.model.Account;
import com.bank.account.model.AccountMovement;
import com.bank.account.model.MovementType;
import com.bank.account.repository.AccountMovementRepository;
import com.bank.account.repository.AccountRepository;
import com.bank.account.service.creation.AccountCreationRule;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

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
    private final List<AccountCreationRule> creationRules;
    private final AccountLedger accountLedger;

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
                            AccountCreationStrategy strategy = creationStrategyFactory.resolve(
                                    request.getAccountType(), customer.getCustomerProfile());
                            return Flux.fromIterable(creationRules)
                                    .concatMap(rule -> rule.validate(customer, request))
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
        return RxJava3Adapter.monoToSingle(findAccount(id)
                .flatMap(account -> accountLedger.apply(account, request.getAmount(), MovementType.DEPOSIT, null))
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
        return RxJava3Adapter.monoToSingle(findAccount(id)
                .flatMap(account -> accountLedger.apply(account, request.getAmount(), MovementType.WITHDRAWAL, null))
                .map(AccountMapper::toResponse));
    }

    /**
     * Aggregates account movements by product for a date range.
     *
     * @param from inclusive start
     * @param to   exclusive end
     * @return product metrics
     */
    public Observable<ProductMetricDto> productReport(Instant from, Instant to) {
        return RxJava3Adapter.fluxToObservable(
                Mono.zip(accountRepository.findAll().collectList(),
                                movementRepository.findInRange(from, to)
                                        .collectList())
                        .flatMapMany(tuple -> {
                            List<Account> accounts = tuple.getT1();
                            List<AccountMovement> movements = tuple.getT2();
                            return Flux.fromIterable(accounts.stream()
                                    .collect(Collectors.groupingBy(
                                            account -> account.getAccountType() + "-" + account.getProductVariant(),
                                            Collectors.toList()))
                                    .entrySet()
                                    .stream()
                                    .map(entry -> toMetric(entry.getValue(), movements))
                                    .collect(Collectors.toList()));
                        })
        );
    }

    private ProductMetricDto toMetric(List<Account> accounts, List<AccountMovement> movements) {
        Account sample = accounts.get(0);
        List<String> ids = accounts.stream().map(Account::getId).collect(Collectors.toList());
        List<AccountMovement> related = movements.stream()
                .filter(movement -> ids.contains(movement.getAccountId()))
                .collect(Collectors.toList());
        BigDecimal amount = related.stream()
                .map(AccountMovement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal commissions = related.stream()
                .map(movement -> movement.getCommissionAmount() == null
                        ? BigDecimal.ZERO : movement.getCommissionAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return ProductMetricDto.builder()
                .product(sample.getAccountType() + "-" + sample.getProductVariant())
                .accountType(sample.getAccountType())
                .variant(sample.getProductVariant())
                .count(accounts.size())
                .movementCount(related.size())
                .movementAmount(amount)
                .commissions(commissions)
                .build();
    }

    Mono<Account> findAccount(String id) {
        return accountRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException("Account not found", HttpStatus.NOT_FOUND)));
    }
}
