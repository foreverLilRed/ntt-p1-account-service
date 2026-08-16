package com.bank.account.service;

import com.bank.account.model.MovementType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionCommissionCalculatorDebitTest {

    @Test
    void debitPaymentIsBillable() {
        TransactionCommissionCalculator calculator = new TransactionCommissionCalculator();
        assertTrue(calculator.isBillable(MovementType.DEBIT_PAYMENT));
        assertEquals(new BigDecimal("2.50"), calculator.calculate(5, 5, new BigDecimal("2.50")));
    }
}
