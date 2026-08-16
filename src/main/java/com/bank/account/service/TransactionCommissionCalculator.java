package com.bank.account.service;

import com.bank.account.model.MovementType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

/**
 * Calculates commission once the free monthly transaction quota is exceeded.
 */
@Component
public class TransactionCommissionCalculator {

    private static final Set<MovementType> BILLABLE = EnumSet.of(
            MovementType.DEPOSIT, MovementType.WITHDRAWAL, MovementType.TRANSFER_OUT, MovementType.DEBIT_PAYMENT);

    /**
     * Returns the fee for the next billable movement.
     *
     * @param billedCountThisMonth billable movements already posted this month
     * @param freeLimit            free movements allowed
     * @param fee                  commission per extra movement
     * @return commission amount (zero when still within the free quota)
     */
    public BigDecimal calculate(long billedCountThisMonth, Integer freeLimit, BigDecimal fee) {
        int limit = freeLimit == null ? Integer.MAX_VALUE : freeLimit;
        BigDecimal commission = fee == null ? BigDecimal.ZERO : fee;
        return billedCountThisMonth >= limit ? commission : BigDecimal.ZERO;
    }

    /**
     * Whether a movement type consumes the free monthly quota.
     *
     * @param type movement type
     * @return true when the type is billable
     */
    public boolean isBillable(MovementType type) {
        return type != null && BILLABLE.contains(type);
    }
}
