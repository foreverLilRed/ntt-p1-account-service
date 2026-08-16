package com.bank.account.controller;

import com.bank.account.dto.AccountRequest;
import com.bank.account.dto.AccountResponse;
import com.bank.account.dto.BalanceResponse;
import com.bank.account.dto.TransactionRequest;
import com.bank.account.service.AccountService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for account CRUD and transactional operations.
 */
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Passive product management API")
public class AccountController {

    private final AccountService accountService;

    /**
     * Creates an account.
     *
     * @param request create payload
     * @return created account
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create account")
    public Single<AccountResponse> create(@Valid @RequestBody AccountRequest request) {
        return accountService.create(request);
    }

    /**
     * Lists all accounts.
     *
     * @return account stream
     */
    @GetMapping
    @Operation(summary = "List accounts")
    public Observable<AccountResponse> findAll() {
        return accountService.findAll();
    }

    /**
     * Retrieves an account by id.
     *
     * @param id account id
     * @return account
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get account by id")
    public Single<AccountResponse> findById(@PathVariable String id) {
        return accountService.findById(id);
    }

    /**
     * Updates an account.
     *
     * @param id      account id
     * @param request update payload
     * @return updated account
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update account")
    public Single<AccountResponse> update(@PathVariable String id,
                                          @Valid @RequestBody AccountRequest request) {
        return accountService.update(id, request);
    }

    /**
     * Deletes an account.
     *
     * @param id account id
     * @return completion signal
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete account")
    public Completable delete(@PathVariable String id) {
        return accountService.delete(id);
    }

    /**
     * Returns available balance.
     *
     * @param id account id
     * @return balance
     */
    @GetMapping("/{id}/balance")
    @Operation(summary = "Get account balance")
    public Single<BalanceResponse> getBalance(@PathVariable String id) {
        return accountService.getBalance(id);
    }

    /**
     * Deposits funds into an account.
     *
     * @param id      account id
     * @param request transaction payload
     * @return updated account
     */
    @PostMapping("/{id}/deposits")
    @Operation(summary = "Deposit into account")
    public Single<AccountResponse> deposit(@PathVariable String id,
                                           @Valid @RequestBody TransactionRequest request) {
        return accountService.deposit(id, request);
    }

    /**
     * Withdraws funds from an account.
     *
     * @param id      account id
     * @param request transaction payload
     * @return updated account
     */
    @PostMapping("/{id}/withdrawals")
    @Operation(summary = "Withdraw from account")
    public Single<AccountResponse> withdraw(@PathVariable String id,
                                            @Valid @RequestBody TransactionRequest request) {
        return accountService.withdraw(id, request);
    }
}
