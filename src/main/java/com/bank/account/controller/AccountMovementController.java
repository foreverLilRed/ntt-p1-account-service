package com.bank.account.controller;

import com.bank.account.dto.AccountMovementRequest;
import com.bank.account.dto.AccountMovementResponse;
import com.bank.account.service.AccountMovementService;
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
 * REST controller exposing account movement CRUD and product movement queries.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Account Movements", description = "Account movement management API")
public class AccountMovementController {

    private final AccountMovementService movementService;

    /**
     * Creates a movement record.
     *
     * @param request create payload
     * @return created movement
     */
    @PostMapping("/api/v1/account-movements")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create account movement")
    public Single<AccountMovementResponse> create(@Valid @RequestBody AccountMovementRequest request) {
        return movementService.create(request);
    }

    /**
     * Lists all movements.
     *
     * @return movement stream
     */
    @GetMapping("/api/v1/account-movements")
    @Operation(summary = "List account movements")
    public Observable<AccountMovementResponse> findAll() {
        return movementService.findAll();
    }

    /**
     * Retrieves a movement by id.
     *
     * @param id movement id
     * @return movement
     */
    @GetMapping("/api/v1/account-movements/{id}")
    @Operation(summary = "Get account movement by id")
    public Single<AccountMovementResponse> findById(@PathVariable String id) {
        return movementService.findById(id);
    }

    /**
     * Updates a movement.
     *
     * @param id      movement id
     * @param request update payload
     * @return updated movement
     */
    @PutMapping("/api/v1/account-movements/{id}")
    @Operation(summary = "Update account movement")
    public Single<AccountMovementResponse> update(@PathVariable String id,
                                                  @Valid @RequestBody AccountMovementRequest request) {
        return movementService.update(id, request);
    }

    /**
     * Deletes a movement.
     *
     * @param id movement id
     * @return completion signal
     */
    @DeleteMapping("/api/v1/account-movements/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete account movement")
    public Completable delete(@PathVariable String id) {
        return movementService.delete(id);
    }

    /**
     * Lists all movements of a bank product (account).
     *
     * @param accountId account id
     * @return movement stream
     */
    @GetMapping("/api/v1/accounts/{accountId}/movements")
    @Operation(summary = "List movements of an account")
    public Observable<AccountMovementResponse> findByAccount(@PathVariable String accountId) {
        return movementService.findByAccountId(accountId);
    }
}
