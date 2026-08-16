package com.bank.account.service;

import com.bank.account.model.AccountMovement;
import com.bank.account.model.MovementType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DailyAverageCalculatorTest {

    private final DailyAverageCalculator calculator = new DailyAverageCalculator();

    @Test
    void averageIncludesTodayProjectedWithdrawal() {
        LocalDate today = LocalDate.of(2026, 8, 3);
        AccountMovement deposit = AccountMovement.builder()
                .movementType(MovementType.DEPOSIT)
                .amount(new BigDecimal("300"))
                .commissionAmount(BigDecimal.ZERO)
                .occurredAt(today.minusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC))
                .build();
        BigDecimal average = calculator.monthToDateAverage(
                new BigDecimal("300"), List.of(deposit), new BigDecimal("-50"), today);
        assertTrue(average.compareTo(BigDecimal.ZERO) > 0);
    }
}
