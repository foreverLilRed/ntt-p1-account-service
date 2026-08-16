package com.bank.account.service;

import com.bank.account.exception.BusinessException;
import com.bank.account.model.Account;
import com.bank.account.model.AccountMovement;
import com.bank.account.model.AccountProductVariant;
import com.bank.account.model.AccountStatus;
import com.bank.account.model.AccountType;
import com.bank.account.model.MovementType;
import com.bank.account.repository.AccountMovementRepository;
import com.bank.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * Applies deposits, withdrawals and transfers to an account ledger.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountLedger {

    private final AccountRepository accountRepository;
    private final AccountMovementRepository movementRepository;
    private final TransactionCommissionCalculator commissionCalculator;
    private final DailyAverageCalculator dailyAverageCalculator;

    /**
     * Posts a movement against the given account.
     *
     * @param account    loaded account
     * @param amount     absolute amount
     * @param type       movement type
     * @param transferId optional transfer correlation
     * @return persisted account
     */
    public Mono<Account> apply(Account account, BigDecimal amount, MovementType type, String transferId) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            return Mono.error(new BusinessException("Account is not active", HttpStatus.BAD_REQUEST));
        }
        InstantRange month = currentMonth();
        return validateFixedTerm(account, month)
                .then(movementRepository
                        .findByAccountIdInRange(account.getId(), month.from(), month.to())
                        .collectList())
                .flatMap(movements -> post(account, amount, type, transferId, movements));
    }

    private Mono<Account> post(Account account, BigDecimal amount, MovementType type,
                               String transferId, List<AccountMovement> movements) {
        long billed = movements.stream().filter(item -> commissionCalculator.isBillable(item.getMovementType())).count();
        BigDecimal commission = commissionCalculator.isBillable(type)
                ? commissionCalculator.calculate(billed, account.getFreeMonthlyTransactions(),
                account.getTransactionCommissionFee())
                : BigDecimal.ZERO;

        BigDecimal signed = isCredit(type) ? amount : amount.negate();
        BigDecimal projected = account.getBalance().add(signed).subtract(commission);
        if (projected.compareTo(BigDecimal.ZERO) < 0) {
            return Mono.error(new BusinessException("Insufficient funds", HttpStatus.BAD_REQUEST));
        }

        if (account.getProductVariant() == AccountProductVariant.VIP
                && account.getAccountType() == AccountType.SAVINGS
                && account.getMinDailyAverageBalance() != null
                && !isCredit(type)) {
            BigDecimal average = dailyAverageCalculator.monthToDateAverage(
                    account.getBalance(), movements, signed.subtract(commission),
                    LocalDate.now(ZoneOffset.UTC));
            if (average.compareTo(account.getMinDailyAverageBalance()) < 0) {
                return Mono.error(new BusinessException(
                        "VIP savings daily average would fall below "
                                + account.getMinDailyAverageBalance(),
                        HttpStatus.BAD_REQUEST));
            }
        }

        Instant now = Instant.now();
        account.setBalance(projected);
        account.setUpdatedAt(now);
        AccountMovement movement = AccountMovement.builder()
                .accountId(account.getId())
                .customerId(account.getCustomerId())
                .movementType(type)
                .amount(amount)
                .commissionAmount(commission)
                .transferId(transferId)
                .balanceAfter(projected)
                .occurredAt(now)
                .build();
        log.info("Posting {} of {} commission={} on accountId={}", type, amount, commission, account.getId());
        return movementRepository.save(movement).then(accountRepository.save(account));
    }

    private Mono<Void> validateFixedTerm(Account account, InstantRange month) {
        if (account.getAccountType() != AccountType.FIXED_TERM) {
            return Mono.empty();
        }
        int today = LocalDate.now(ZoneOffset.UTC).getDayOfMonth();
        if (account.getAllowedTransactionDay() == null || today != account.getAllowedTransactionDay()) {
            return Mono.error(new BusinessException(
                    "FIXED_TERM accounts only allow movements on day "
                            + account.getAllowedTransactionDay() + " of the month",
                    HttpStatus.BAD_REQUEST));
        }
        return movementRepository.countByAccountIdInRange(
                        account.getId(), month.from(), month.to())
                .flatMap(count -> {
                    if (count >= 1) {
                        return Mono.error(new BusinessException(
                                "FIXED_TERM accounts allow only one movement per month",
                                HttpStatus.BAD_REQUEST));
                    }
                    return Mono.empty();
                });
    }

    private boolean isCredit(MovementType type) {
        return type == MovementType.DEPOSIT || type == MovementType.TRANSFER_IN;
    }

    private InstantRange currentMonth() {
        LocalDate start = LocalDate.now(ZoneOffset.UTC).with(TemporalAdjusters.firstDayOfMonth());
        Instant from = start.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = start.plusMonths(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return new InstantRange(from, to);
    }

    private record InstantRange(Instant from, Instant to) {
    }
}
