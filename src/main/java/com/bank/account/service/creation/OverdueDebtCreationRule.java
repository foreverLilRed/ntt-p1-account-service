package com.bank.account.service.creation;

import com.bank.account.dto.AccountRequest;
import com.bank.account.dto.CustomerDto;
import com.bank.account.exception.BusinessException;
import com.bank.account.service.DebtStatusStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Blocks account opening when the customer has overdue credit debt.
 */
@Component
@RequiredArgsConstructor
public class OverdueDebtCreationRule implements AccountCreationRule {

    private final DebtStatusStore debtStatusStore;

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> validate(CustomerDto customer, AccountRequest request) {
        return debtStatusStore.hasOverdueDebt(customer.getId())
                .flatMap(overdue -> {
                    if (Boolean.TRUE.equals(overdue)) {
                        return Mono.error(new BusinessException(
                                "Customer has overdue credit debt and cannot acquire a new product",
                                HttpStatus.BAD_REQUEST));
                    }
                    return Mono.empty();
                });
    }
}
