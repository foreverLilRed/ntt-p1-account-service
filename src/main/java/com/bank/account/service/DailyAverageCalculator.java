package com.bank.account.service;

import com.bank.account.model.AccountMovement;
import com.bank.account.model.MovementType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Computes the month-to-date daily average balance using the Streams API.
 */
@Component
public class DailyAverageCalculator {

    /**
     * Calculates the average of end-of-day balances from month start through today.
     *
     * @param currentBalance current ledger balance (before the projected movement)
     * @param movements      movements already posted this month
     * @param projectedDelta signed amount that will be applied today (may be zero)
     * @param today          evaluation day (UTC)
     * @return daily average
     */
    public BigDecimal monthToDateAverage(BigDecimal currentBalance,
                                         List<AccountMovement> movements,
                                         BigDecimal projectedDelta,
                                         LocalDate today) {
        LocalDate monthStart = today.withDayOfMonth(1);
        BigDecimal netThisMonth = movements.stream()
                .map(this::signedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal startOfMonth = currentBalance.subtract(netThisMonth);

        Map<LocalDate, BigDecimal> byDay = movements.stream()
                .collect(Collectors.groupingBy(
                        movement -> toDate(movement.getOccurredAt()),
                        Collectors.reducing(BigDecimal.ZERO, this::signedAmount, BigDecimal::add)));

        BigDecimal running = startOfMonth;
        BigDecimal sum = BigDecimal.ZERO;
        int days = 0;
        LocalDate cursor = monthStart;
        while (!cursor.isAfter(today)) {
            running = running.add(byDay.getOrDefault(cursor, BigDecimal.ZERO));
            if (cursor.equals(today)) {
                running = running.add(projectedDelta);
            }
            sum = sum.add(running);
            days++;
            cursor = cursor.plusDays(1);
        }
        if (days == 0) {
            return currentBalance.add(projectedDelta);
        }
        return sum.divide(BigDecimal.valueOf(days), 2, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal signedAmount(AccountMovement movement) {
        BigDecimal amount = movement.getAmount() == null ? BigDecimal.ZERO : movement.getAmount();
        BigDecimal commission = movement.getCommissionAmount() == null
                ? BigDecimal.ZERO : movement.getCommissionAmount();
        MovementType type = movement.getMovementType();
        if (type == MovementType.DEPOSIT || type == MovementType.TRANSFER_IN) {
            return amount.subtract(commission);
        }
        if (type == MovementType.WITHDRAWAL || type == MovementType.TRANSFER_OUT
                || type == MovementType.COMMISSION) {
            return amount.add(commission).negate();
        }
        return BigDecimal.ZERO;
    }

    private LocalDate toDate(Instant instant) {
        return instant == null ? LocalDate.now(ZoneOffset.UTC) : instant.atZone(ZoneOffset.UTC).toLocalDate();
    }
}
