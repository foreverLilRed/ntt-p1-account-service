package com.bank.account.mapper;

import com.bank.account.dto.AccountMovementRequest;
import com.bank.account.dto.AccountMovementResponse;
import com.bank.account.dto.AccountRequest;
import com.bank.account.dto.AccountResponse;
import com.bank.account.dto.BalanceResponse;
import com.bank.account.model.Account;
import com.bank.account.model.AccountMovement;
import com.bank.account.model.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Mapping helpers for account and movement models.
 */
public final class AccountMapper {

    private AccountMapper() {
    }

    /**
     * Builds a new account entity from a create request and product defaults.
     *
     * @param request             create payload
     * @param maintenanceFee      fee applied to checking accounts
     * @param monthlyMovementLimit monthly limit for savings accounts
     * @return new account
     */
    public static Account toEntity(AccountRequest request,
                                   BigDecimal maintenanceFee,
                                   Integer monthlyMovementLimit) {
        Instant now = Instant.now();
        List<String> holders = Optional.ofNullable(request.getHolders())
                .filter(list -> !list.isEmpty())
                .orElseGet(() -> List.of(request.getCustomerId()));
        List<String> signers = Optional.ofNullable(request.getAuthorizedSigners())
                .orElseGet(ArrayList::new);

        return Account.builder()
                .customerId(request.getCustomerId())
                .accountType(request.getAccountType())
                .balance(BigDecimal.ZERO)
                .maintenanceFee(maintenanceFee)
                .monthlyMovementLimit(monthlyMovementLimit)
                .allowedTransactionDay(request.getAllowedTransactionDay())
                .holders(new ArrayList<>(holders))
                .authorizedSigners(new ArrayList<>(signers))
                .status(Optional.ofNullable(request.getStatus()).orElse(AccountStatus.ACTIVE))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Applies update fields to an existing account.
     *
     * @param account existing entity
     * @param request update payload
     * @return mutated entity
     */
    public static Account applyUpdate(Account account, AccountRequest request) {
        account.setHolders(Optional.ofNullable(request.getHolders()).orElse(account.getHolders()));
        account.setAuthorizedSigners(Optional.ofNullable(request.getAuthorizedSigners())
                .orElse(account.getAuthorizedSigners()));
        account.setAllowedTransactionDay(request.getAllowedTransactionDay() != null
                ? request.getAllowedTransactionDay()
                : account.getAllowedTransactionDay());
        if (request.getStatus() != null) {
            account.setStatus(request.getStatus());
        }
        account.setUpdatedAt(Instant.now());
        return account;
    }

    /**
     * Maps an account entity to the API response.
     *
     * @param account persistence entity
     * @return response DTO
     */
    public static AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .customerId(account.getCustomerId())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .maintenanceFee(account.getMaintenanceFee())
                .monthlyMovementLimit(account.getMonthlyMovementLimit())
                .allowedTransactionDay(account.getAllowedTransactionDay())
                .holders(account.getHolders())
                .authorizedSigners(account.getAuthorizedSigners())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    /**
     * Maps an account to a balance response.
     *
     * @param account persistence entity
     * @return balance DTO
     */
    public static BalanceResponse toBalance(Account account) {
        return BalanceResponse.builder()
                .accountId(account.getId())
                .customerId(account.getCustomerId())
                .accountType(account.getAccountType())
                .availableBalance(account.getBalance())
                .build();
    }

    /**
     * Maps a movement request into a persistence entity.
     *
     * @param request create payload
     * @return movement entity
     */
    public static AccountMovement toMovementEntity(AccountMovementRequest request) {
        return AccountMovement.builder()
                .accountId(request.getAccountId())
                .customerId(request.getCustomerId())
                .movementType(request.getMovementType())
                .amount(request.getAmount())
                .balanceAfter(BigDecimal.ZERO)
                .occurredAt(Instant.now())
                .build();
    }

    /**
     * Maps a movement entity to the API response.
     *
     * @param movement persistence entity
     * @return response DTO
     */
    public static AccountMovementResponse toMovementResponse(AccountMovement movement) {
        return AccountMovementResponse.builder()
                .id(movement.getId())
                .accountId(movement.getAccountId())
                .customerId(movement.getCustomerId())
                .movementType(movement.getMovementType())
                .amount(movement.getAmount())
                .balanceAfter(movement.getBalanceAfter())
                .occurredAt(movement.getOccurredAt())
                .build();
    }
}
