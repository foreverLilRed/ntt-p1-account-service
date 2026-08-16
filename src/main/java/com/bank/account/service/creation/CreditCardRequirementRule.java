package com.bank.account.service.creation;

import com.bank.account.client.CreditClient;
import com.bank.account.dto.AccountRequest;
import com.bank.account.dto.CustomerDto;
import com.bank.account.exception.BusinessException;
import com.bank.account.model.AccountType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * RP2-002 / RP2-004: VIP savings and PYME checking require an active credit card.
 */
@Component
@RequiredArgsConstructor
public class CreditCardRequirementRule implements AccountCreationRule {

    private final CreditClient creditClient;

    @Override
    public Mono<Void> validate(CustomerDto customer, AccountRequest request) {
        boolean vipSavings = "VIP".equalsIgnoreCase(customer.getCustomerProfile())
                && request.getAccountType() == AccountType.SAVINGS;
        boolean pymeChecking = "PYME".equalsIgnoreCase(customer.getCustomerProfile())
                && request.getAccountType() == AccountType.CHECKING;
        if (!vipSavings && !pymeChecking) {
            return Mono.empty();
        }
        return creditClient.hasActiveCreditCard(customer.getId())
                .flatMap(hasCard -> {
                    if (Boolean.FALSE.equals(hasCard)) {
                        return Mono.error(new BusinessException(
                                "An active credit card is required to open this product",
                                HttpStatus.BAD_REQUEST));
                    }
                    return Mono.empty();
                });
    }
}
