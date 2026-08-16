package com.bank.account.service;

import com.bank.account.model.MovementType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionCommissionCalculatorTest {

    private final TransactionCommissionCalculator calculator = new TransactionCommissionCalculator();

    @Test
    void noCommissionWithinFreeQuota() {
        assertEquals(BigDecimal.ZERO, calculator.calculate(4, 5, new BigDecimal("2.50")));
    }

    @Test
    void chargesCommissionAfterFreeQuota() {
        assertEquals(new BigDecimal("2.50"), calculator.calculate(5, 5, new BigDecimal("2.50")));
    }

    @Test
    void sixthMovementPaysCommission() {
        assertEquals(new BigDecimal("2.50"), calculator.calculate(5, 5, new BigDecimal("2.50")));
        assertEquals(BigDecimal.ZERO, calculator.calculate(0, 5, new BigDecimal("2.50")));
    }

    @Test
    void depositIsBillable() {
        assertTrue(calculator.isBillable(MovementType.DEPOSIT));
        assertTrue(calculator.isBillable(MovementType.WITHDRAWAL));
        assertTrue(calculator.isBillable(MovementType.TRANSFER_OUT));
        assertFalse(calculator.isBillable(MovementType.TRANSFER_IN));
        assertFalse(calculator.isBillable(MovementType.COMMISSION));
    }
}
