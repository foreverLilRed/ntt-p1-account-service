package com.bank.account.service.creation;

import com.bank.account.dto.AccountRequest;
import com.bank.account.exception.BusinessException;
import com.bank.account.mapper.AccountMapper;
import com.bank.account.model.Account;
import com.bank.account.model.AccountType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Creation strategy for FIXED_TERM accounts.
 */
@Component
public class FixedTermAccountCreationStrategy implements AccountCreationStrategy {

    @Override
    public AccountType supportedType() {
        return AccountType.FIXED_TERM;
    }

    @Override
    public Mono<Void> validateProductRules(AccountRequest request) {
        Integer allowedDay = request.getAllowedTransactionDay();
        if (allowedDay == null || allowedDay < 1 || allowedDay > 31) {
            return Mono.error(new BusinessException(
                    "FIXED_TERM requires allowedTransactionDay between 1 and 31",
                    HttpStatus.BAD_REQUEST));
        }
        return Mono.empty();
    }

    @Override
    public Account buildEntity(AccountRequest request) {
        return AccountMapper.toEntity(request, BigDecimal.ZERO, null);
    }
}
